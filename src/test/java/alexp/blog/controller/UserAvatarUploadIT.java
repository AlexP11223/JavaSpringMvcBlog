package alexp.blog.controller;

import alexp.blog.AbstractIntegrationTest;
import alexp.blog.service.AvatarServiceImpl;
import alexp.blog.service.FileNameGenerator;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import org.apache.commons.io.FileUtils;
import org.aspectj.lang.annotation.After;
import org.junit.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static alexp.blog.utils.SecurityUtils.userBob;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@DatabaseSetup("data.xml")
public class UserAvatarUploadIT extends AbstractIntegrationTest {

    @Autowired
    private AvatarServiceImpl avatarService;

    @Value("${uploading.dirpath}")
    private String uploadingDirPath;

    @Override
    protected void setupTest() {
        FileNameGenerator fileNameGenerator = Mockito.mock(FileNameGenerator.class);
        avatarService.setFileNameGenerator(fileNameGenerator);

        when(fileNameGenerator.getFileName(Matchers.anyString(), Matchers.anyString())).thenReturn("generatedName");
    }

    @org.junit.After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(new File(uploadingDirPath));
    }

    @Test
    @ExpectedDatabase("data-avatar-added.xml")
    public void shouldUploadAvatar() throws Exception {
        MockMultipartFile img = new MockMultipartFile("avatarFile", "img.jpg", "image/jpeg", this.getClass().getResourceAsStream("img.jpg"));

        mockMvc.perform(fileUpload("/upload_avatar").file(img).with(userBob()).with(csrf().asHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ok")))
                .andExpect(jsonPath("$.link", is("generatedName_big.jpg")));

        assertThat(new File(uploadingDirPath + "generatedName_big.jpg").exists(), is(equalTo(true)));
        assertThat(new File(uploadingDirPath + "generatedName_small.jpg").exists(), is(equalTo(true)));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldNotUploadNotImage() throws Exception {
        MockMultipartFile img = new MockMultipartFile("avatarFile", "notimg.exe", "image/jpeg", "something".getBytes());

        mockMvc.perform(fileUpload("/upload_avatar").file(img).with(userBob()).with(csrf().asHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("invalid_format")));

        assertThat(new File(uploadingDirPath + "generatedName_big.jpg").exists(), is(equalTo(false)));
        assertThat(new File(uploadingDirPath + "generatedName_small.jpg").exists(), is(equalTo(false)));
    }

    @Test
    @ExpectedDatabase("data.xml")
    @DatabaseSetup("data-avatar-added.xml")
    public void shouldRemoveAvatar() throws Exception {
        mockMvc.perform(post("/remove_avatar").with(userBob()).with(csrf().asHeader()))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

}