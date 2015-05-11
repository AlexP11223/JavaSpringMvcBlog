package alexp.blog.service;

import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@Service("avatarService")
public class AvatarServiceImpl implements AvatarService {

    @Value("${uploading.dirpath}")
    private String uploadingDirPath;

    @Autowired
    private FileNameGenerator fileNameGenerator;

    public AvatarServiceImpl() {
        ImageIO.setUseCache(false);
    }

    public final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

    @Override
    public UploadedAvatarInfo upload(MultipartFile file) throws IOException, UnsupportedFormatException {
        String fileName = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(fileName).toLowerCase();

        if (!SUPPORTED_EXTENSIONS.contains(ext))
            throw new UnsupportedFormatException(fileName);

        String name = fileNameGenerator.getFileName(fileName, "avatar");
        String bigImageName = name + "_big" + "." + ext;
        String smallImageName = name + "_small" + "." + ext;

        BufferedImage image = ImageIO.read(file.getInputStream());

        BufferedImage bigImage = resize(image, 160);

        new File(uploadingDirPath).mkdirs();

        ImageIO.write(bigImage, ext, new File(uploadingDirPath + bigImageName));

        BufferedImage smallImage = resize(image, 28);

        ImageIO.write(smallImage, ext, new File(uploadingDirPath + smallImageName));

        return new UploadedAvatarInfo(bigImageName, smallImageName);
    }

    private BufferedImage resize(BufferedImage image, int size) {
        BufferedImage result = Scalr.resize(image, Scalr.Mode.FIT_EXACT, size, size);

        // would be better to crop image if not square, instead of just resizing without preserving proportions

        return result;
    }

    public FileNameGenerator getFileNameGenerator() {
        return fileNameGenerator;
    }

    public void setFileNameGenerator(FileNameGenerator fileNameGenerator) {
        this.fileNameGenerator = fileNameGenerator;
    }
}
