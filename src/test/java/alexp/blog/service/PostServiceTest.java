package alexp.blog.service;

import alexp.blog.model.Post;
import alexp.blog.model.Tag;
import alexp.blog.repository.PostRepository;
import alexp.blog.repository.TagRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetPostPage() {
        final int pageSize = 10;

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Post post = new Post();
            post.setId((long) i);
            posts.add(post);
        }

        when(postRepository.findAll(Matchers.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        when(postRepository.findAll(Matchers.eq(new PageRequest(0, pageSize, Sort.Direction.DESC, "dateTime"))))
                .thenReturn(new PageImpl<>(posts.subList(0, 10)));

        when(postRepository.findAll(new PageRequest(1, pageSize, Sort.Direction.DESC, "dateTime")))
                .thenReturn(new PageImpl<>(posts.subList(10, 15)));

        Page<Post> page1 = postService.getPostsPage(0, pageSize);
        Page<Post> page2 = postService.getPostsPage(1, pageSize);

        assertThat(page1.getNumberOfElements(), is(equalTo(10)));
        assertThat(page2.getNumberOfElements(), is(equalTo(5)));

        verify(postRepository, times(2)).findAll(Matchers.any(PageRequest.class));
    }

    @Test
    public void shouldGetEmptyPostPage() {
        when(postRepository.findAll(Matchers.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        Page<Post> page = postService.getPostsPage(99, 10);

        assertThat(page.getNumberOfElements(), is(equalTo(0)));

        verify(postRepository, times(1)).findAll(Matchers.any(PageRequest.class));
    }

    @Test
    public void shouldGetPost() {
        final long postId = 1L;

        Post post = new Post();
        post.setId(postId);

        when(postRepository.findOne(postId))
                .thenReturn(post);

        Post retrievedPost = postService.getPost(postId);

        assertThat(retrievedPost, is(equalTo(post)));

        verify(postRepository, times(1)).findOne(postId);
    }

    @Test
    public void shouldReturnNullWhenPostNotExists() {
        final long postId = 1L;

        Post retrievedPost = postService.getPost(postId);

        assertThat(retrievedPost, is(equalTo(null)));

        verify(postRepository, times(1)).findOne(postId);
    }

    @Test
    public void shouldAddNewPostAndSetTags() {
        Post post = new Post();
        post.setFullPostText("short text " + Post.shortPartSeparator() + " full text");

        String tags = "c++, hello world";

        postService.saveNewPost(post, tags);

        assertThat(post.getShortTextPart(), containsString("short text"));
        assertThat(post.getShortTextPart(), not(containsString("full text")));

        assertThat(post.getFullPostText(), allOf(containsString("full text"), containsString("full text"), containsString(Post.shortPartSeparator())));

        assertThat(post.getTags().size(), is(equalTo(2)));

        verify(tagRepository, times(2)).findByNameIgnoreCase(Matchers.anyString());
        verify(tagRepository, times(2)).save(Matchers.any(Tag.class));

        verify(postRepository, times(1)).save(Matchers.any(Post.class));
    }
}