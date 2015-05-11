package alexp.blog.service;

public class UploadedAvatarInfo {
    public final String bigImageLink;

    public final String smallImageLink;

    public UploadedAvatarInfo(String bigImageLink, String smallImageLink) {
        this.bigImageLink = bigImageLink;
        this.smallImageLink = smallImageLink;
    }
}
