package alexp.blog.service;

import alexp.blog.model.Post;
import alexp.blog.model.Tag;
import alexp.blog.repository.PostRepository;
import alexp.blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("postService")
public class PostServiceImpl implements PostService
{
    @Autowired
    private PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

    @Override
    public Page<Post> getPostsPage(int pageNumber, int pageSize) {
        return postRepository.findAll(new PageRequest(pageNumber, pageSize, Sort.Direction.DESC, "dateTime"));
    }

    @Override
    public Post getPost(Long id) {
        return postRepository.findOne(id);
    }

    @Override
    public void saveNewPost(Post post, String tagstext) {
        int cutInd = post.getFullPostText().indexOf(Post.shortPartSeparator());
        if (cutInd > 0) {
            post.setShortTextPart(post.getFullPostText().substring(0, cutInd));
        }

        post.setDateTime(new Date());

        String[] tags = tagstext.split(",");

        for (String tagname: tags) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagname.trim());

            if (tag == null) {
                tag = new Tag();
                tag.setName(tagname.trim());
                tagRepository.save(tag);
            }

            post.getTags().add(tag);
        }

        postRepository.saveAndFlush(post);
    }
}
