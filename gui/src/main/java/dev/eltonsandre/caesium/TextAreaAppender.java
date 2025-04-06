package dev.eltonsandre.caesium;

import java.io.Serializable;

import javax.swing.JTextArea;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class TextAreaAppender extends AbstractAppender {

    private JTextArea textarea = null;
    private final int maxLines;

    public TextAreaAppender(String name, Filter filter, Layout<? extends Serializable> layout, int maxLines, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.maxLines = maxLines;
    }

    @Override
    public void append(LogEvent event) {
        PatternLayout layout = (PatternLayout) getLayout();
        textarea.append(layout.toSerializable(event));
        if ((maxLines > 0) && (textarea.getLineCount() > (maxLines + 1))) {
            String text = textarea.getText();
            int pos = text.indexOf('\n');
            text = text.substring(pos + 1);
            textarea.setText(text);
        }
    }

    public void setTextArea(JTextArea textArea) {
        this.textarea = textArea;
    }

}

