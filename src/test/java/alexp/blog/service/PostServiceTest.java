package alexp.blog.service;

import alexp.blog.model.Post;
import alexp.blog.model.PostEditDto;
import alexp.blog.model.Tag;
import alexp.blog.repository.PostRepository;
import alexp.blog.repository.TagRepository;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserService userService;

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

        when(postRepository.findByHiddenFalseOrHiddenIsNull(Matchers.eq(new PageRequest(0, pageSize, Sort.Direction.DESC, "dateTime"))))
                .thenReturn(new PageImpl<>(posts.subList(0, 10)));

        when(postRepository.findByHiddenFalseOrHiddenIsNull(new PageRequest(1, pageSize, Sort.Direction.DESC, "dateTime")))
                .thenReturn(new PageImpl<>(posts.subList(10, 15)));

        Page<Post> page1 = postService.getPostsPage(0, pageSize);
        Page<Post> page2 = postService.getPostsPage(1, pageSize);

        assertThat(page1.getNumberOfElements(), is(equalTo(10)));
        assertThat(page2.getNumberOfElements(), is(equalTo(5)));

        verify(postRepository, times(2)).findByHiddenFalseOrHiddenIsNull(Matchers.any(PageRequest.class));
    }

    @Test
    public void shouldGetEmptyPostPage() {
        when(postRepository.findByHiddenFalseOrHiddenIsNull(Matchers.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        Page<Post> page = postService.getPostsPage(99, 10);

        assertThat(page.getNumberOfElements(), is(equalTo(0)));

        verify(postRepository, times(1)).findByHiddenFalseOrHiddenIsNull(Matchers.any(PageRequest.class));
    }

    @Test
    public void shouldIncludeHiddenPostsIfAdmin() {
        when(userService.isAdmin()).thenReturn(true);

        postService.getPostsPage(1, 10);

        verify(postRepository, times(1)).findAll(Matchers.any(PageRequest.class));
    }

    @Test
    public void shouldNotIncludeHiddenPostsIfNotAdmin() {
        when(userService.isAdmin()).thenReturn(false);

        postService.getPostsPage(1, 10);

        verify(postRepository, times(1)).findByHiddenFalseOrHiddenIsNull(Matchers.any(PageRequest.class));
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
    public void shouldGetPostEditDto() {
        final long postId = 1L;

        Post post = new Post();
        post.setId(postId);
        post.setFullPostText("post text");
        post.getTags().add(new Tag("c++"));
        post.getTags().add(new Tag("hello world"));

        when(postRepository.findOne(postId)).thenReturn(post);

        PostEditDto retrievedPost = postService.getEditablePost(postId);

        assertThat(retrievedPost.getText(), is(equalTo(post.getFullPostText())));
        assertThat(retrievedPost.getTitle(), is(equalTo(post.getTitle())));
        assertThat(retrievedPost.getTags(), is(equalTo("c++, hello world")));

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
    public void shouldAddNewPost() {
        PostEditDto postEditDto = new PostEditDto();
        postEditDto.setText("short text " + Post.shortPartSeparator() + " full text");
        postEditDto.setTags("c++, hello world");

        Post post = postService.saveNewPost(postEditDto);

        assertThat(post.getShortTextPart(), containsString("short text"));
        assertThat(post.getShortTextPart(), not(containsString("full text")));

        assertThat(post.getFullPostText(), allOf(containsString("full text"), containsString("full text"), containsString(Post.shortPartSeparator())));

        assertThat(post.getTags().size(), is(equalTo(2)));

        assertThat(post.isHidden(), CoreMatchers.is(equalTo(false)));

        verify(tagRepository, times(2)).findByNameIgnoreCase(Matchers.anyString());

        verify(postRepository, times(1)).saveAndFlush(Matchers.any(Post.class));
    }

    @Test
    public void shouldEditPost() {
        Date dt = new GregorianCalendar(2015, Calendar.DECEMBER, 1, 12, 30, 0).getTime();

        Long postId = 1L;

        Post oldPost = new Post();
        oldPost.setId(postId);
        oldPost.setDateTime(dt);
        oldPost.getTags().add(new Tag("tag"));

        PostEditDto postEditDto = new PostEditDto();
        postEditDto.setId(postId);
        postEditDto.setText("short text " + Post.shortPartSeparator() + " full text");
        postEditDto.setTags("c++, hello world");

        when(postRepository.findOne(postId)).thenReturn(oldPost);

        Post post = postService.updatePost(postEditDto);

        assertThat(post.getShortTextPart(), containsString("short text"));
        assertThat(post.getShortTextPart(), not(containsString("full text")));

        assertThat(post.getFullPostText(), allOf(containsString("full text"), containsString("full text"), containsString(Post.shortPartSeparator())));

        assertThat(post.getTags().size(), is(equalTo(2)));

        assertThat(post.getDateTime(), is(dt));

        verify(tagRepository, times(2)).findByNameIgnoreCase(Matchers.anyString());

        verify(postRepository, times(1)).saveAndFlush(Matchers.any(Post.class));
    }

    @Test
    public void shouldClearShortPartWhenItRemovedDuringEditing() {
        Long postId = 1L;

        Post oldPost = new Post();
        oldPost.setId(postId);

        PostEditDto postEditDto = new PostEditDto();
        postEditDto.setId(postId);
        postEditDto.setText("text");

        when(postRepository.findOne(postId)).thenReturn(oldPost);

        Post post = postService.updatePost(postEditDto);

        assertThat(post.getShortTextPart(), isEmptyOrNullString());
        assertThat(post.getFullPostText(), is(equalTo("text")));

        verify(postRepository, times(1)).saveAndFlush(Matchers.any(Post.class));
    }

    @Test
    public void shouldAddLinksToShortPart() {
        String text = "Lorem ipsum\n" +
                "hello [google][1] [yandex][2] world\n" +
                "===cut===\n" +
                "full text\n" +
                "\n" +
                "\n" +
                "  [1]: http://www.google.com\n" +
                "  [2]: http://ya.ru";

        String links = "  [1]: http://www.google.com\n" +
                "  [2]: http://ya.ru";

        PostEditDto postEditDto = new PostEditDto();
        postEditDto.setText(text);

        Post post = postService.saveNewPost(postEditDto);

        assertThat(post.getShortTextPart(), containsString(links));
        assertThat(post.getShortTextPart(), not(containsString("full text")));

        assertThat(post.getFullPostText(), allOf(containsString("full text"), containsString(links)));

        verify(postRepository, times(1)).saveAndFlush(Matchers.any(Post.class));
    }

    @Test
    public void shouldHidePost() {
        Long postId = 1L;

        Post post = new Post();
        post.setHidden(false);

        when(postRepository.findOne(postId)).thenReturn(post);

        postService.setPostVisibility(postId, true);

        assertThat(post.isHidden(), is(CoreMatchers.equalTo(true)));

        verify(postRepository, times(1)).saveAndFlush(post);
    }

    @Test
    public void shouldUnhidePost() {
        Long postId = 1L;

        Post post = new Post();
        post.setHidden(true);

        when(postRepository.findOne(postId)).thenReturn(post);

        postService.setPostVisibility(postId, false);

        assertThat(post.isHidden(), is(CoreMatchers.equalTo(false)));

        verify(postRepository, times(1)).saveAndFlush(post);
    }

    @Test
    public void shouldDeletePost() {
        Long postId = 1L;

        Post post = new Post();

        when(postRepository.findOne(postId)).thenReturn(post);

        postService.deletePost(postId);

        verify(postRepository, times(1)).delete(post);
    }
}