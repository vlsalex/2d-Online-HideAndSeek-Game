import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontsData {

    public Font logoFont;
    public Font gameFont;
    public Font pixelFont;

    public FontsData() {
        try {
            InputStream inputLogoFontStream = getClass().getResourceAsStream("Azonix.otf");
            logoFont = Font.createFont(Font.TRUETYPE_FONT, inputLogoFontStream);
            InputStream inputGameFontStream = getClass().getResourceAsStream("YanoneKaffeesatz-Light.ttf");
            gameFont = Font.createFont(Font.TRUETYPE_FONT, inputGameFontStream);
            InputStream inputPixelFontStream = getClass().getResourceAsStream("DeltaruneCyrillic.ttf");
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, inputPixelFontStream);
        }
        catch (FontFormatException | IOException ignored) {}
    }
}
