package alexp.blog.service;

import alexp.blog.model.Post;
import alexp.blog.model.PostEditDto;
import alexp.blog.model.Tag;
import alexp.blog.repository.PostRepository;
import alexp.blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service("postService")
public class PostServiceImpl implements PostService
{
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserService userService;

    @Override
    public Page<Post> getPostsPage(int pageNumber, int pageSize) {
        PageRequest pageRequest = new PageRequest(pageNumber, pageSize, Sort.Direction.DESC, "dateTime");

        if (userService.isAdmin())
            return postRepository.findAll(pageRequest);

        return postRepository.findByHiddenFalse(pageRequest);
    }

    @Override
    public List<Post> getPostsList(int pageNumber, int pageSize) {
        PageRequest pageRequest = new PageRequest(pageNumber, pageSize, Sort.Direction.DESC, "dateTime");

        return postRepository.findByHiddenIs(false, pageRequest);
    }

    @Override
    public Post getPost(Long id) {
        return postRepository.findOne(id);
    }

    @Override
    public PostEditDto getEditablePost(Long id) {
        Post post = getPost(id);

        if (post == null)
            return null;

        return convertToPostEditDto(post);
    }

    @Override
    public Page<Post> findPostsByTag(List<String> tags, int pageNumber, int pageSize) {
        tags = tags.stream().map(String::toLowerCase).collect(Collectors.toList());

        PageRequest pageRequest = new PageRequest(pageNumber, pageSize, Sort.Direction.DESC, "dateTime");

        if (userService.isAdmin())
            return postRepository.findByTags(tags, (long) tags.size(), pageRequest);

        return postRepository.findByTagsAndNotHidden(tags, (long) tags.size(), pageRequest);
    }

    @Override
    public Post saveNewPost(PostEditDto postEditDto) {
        Post post = new Post();

        fillPost(post, postEditDto);

        post.setDateTime(LocalDateTime.now());

        postRepository.saveAndFlush(post);

        return post;
    }

    @Override
    public Post updatePost(PostEditDto postEditDto) {
        if (postEditDto.getId() == null)
            throw new IllegalArgumentException("cannot update without id");

        Post post = getPost(postEditDto.getId());

        fillPost(post, postEditDto);

        postRepository.saveAndFlush(post);

        return post;
    }

    @Override
    public void setPostVisibility(Long postId, boolean hide) {
        Post post = getPost(postId);

        post.setHidden(hide);

        postRepository.saveAndFlush(post);
    }

    @Override
    public void deletePost(Long postId) {
        Post post = getPost(postId);

        postRepository.delete(post);

        postRepository.flush();
    }

    private PostEditDto convertToPostEditDto(Post post) {
        PostEditDto postEditDto = new PostEditDto();

        postEditDto.setId(post.getId());
        postEditDto.setText(post.getFullPostText());
        postEditDto.setTitle(post.getTitle());

        postEditDto.setTags(post.getTags().stream().map(Tag::getName).collect(Collectors.joining(", ")));

        return postEditDto;
    }

    private void fillPost(Post post, PostEditDto postEditDto) {
        post.setId(postEditDto.getId());
        post.setTitle(postEditDto.getTitle());
        post.setFullPostText(postEditDto.getText());

        int cutInd = postEditDto.getText().indexOf(Post.shortPartSeparator());
        if (cutInd > 0) {
            String shortText = postEditDto.getText().substring(0, cutInd);

            List<String> links = MarkdownConverter.extractLinks(postEditDto.getText());

            if (!links.isEmpty()) {
                shortText += "\n" + links.stream().collect(Collectors.joining("\n"));
            }

            post.setShortTextPart(shortText);
        }
        else {
            post.setShortTextPart(null);
        }

        post.getTags().clear();

        String[] tags = Arrays.stream(postEditDto.getTags().split(",")).map(String::trim).toArray(String[]::new);

        for (String tagname: tags) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagname);

            if (tag == null) {
                tag = new Tag(tagname);
            }

            post.getTags().add(tag);
        }
    }
}
