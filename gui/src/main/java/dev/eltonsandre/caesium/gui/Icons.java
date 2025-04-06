package dev.eltonsandre.caesium.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.*;
import java.text.MessageFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Icons {


    private static Icon load(final String path) {
        try {
           return new FlatSVGIcon(path);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Icon loadIconSvgByTheme(final String iconName) {
        return load(MessageFormat.format("icons/{0}{1}", iconName, isDarkTheme() ? "_dark.svg" : ".svg"));
    }

    private static boolean isDarkTheme() {
        return UIManager.getLookAndFeel().getName().contains("Darcula");
    }

}
