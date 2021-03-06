package org.leibnizcenter.rechtspraak.leibnizannotations;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;

/**
 * Created by maarten on 29-3-16.
 */
public class CellArea extends DefaultTableCellRenderer {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected int rowIndex;
    protected int columnIndex;
    protected JTable table;
    protected Font font;
    private String text;
    private int paragraphStart, paragraphEnd;
    private LineBreakMeasurer lineMeasurer;

    public CellArea(String s, JTable tab, int row, int column, boolean isSelected) {
        text = s;
        rowIndex = row;
        columnIndex = column;
        table = tab;
        font = table.getFont();
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        }
    }

    public void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        if (text != null && !text.isEmpty()) {
            Graphics2D g = (Graphics2D) gr;
            if (lineMeasurer == null) {
                AttributedCharacterIterator paragraph = new AttributedString(text).getIterator();
                paragraphStart = paragraph.getBeginIndex();
                paragraphEnd = paragraph.getEndIndex();
                FontRenderContext frc = g.getFontRenderContext();
                lineMeasurer = new LineBreakMeasurer(paragraph, BreakIterator.getWordInstance(), frc);
            }
            float breakWidth = (float) table.getColumnModel().getColumn(columnIndex).getWidth();
            float drawPosY = 0;
            // Set position to the index of the first character in the paragraph.
            lineMeasurer.setPosition(paragraphStart);
            // Get lines until the entire paragraph has been displayed.
            while (lineMeasurer.getPosition() < paragraphEnd) {
                // Retrieve next layout. A cleverer program would also cache
                // these layouts until the component is re-sized.
                TextLayout layout = lineMeasurer.nextLayout(breakWidth);
                // Compute pen x position. If the paragraph is right-to-left we
                // will align the TextLayouts to the right edge of the panel.
                // Note: this won't occur for the English text in this sample.
                // Note: drawPosX is always where the LEFT of the text is placed.
                float drawPosX = layout.isLeftToRight()
                        ? 0 : breakWidth - layout.getAdvance();
                // Move y-coordinate by the ascent of the layout.
                drawPosY += layout.getAscent();
                // Draw the TextLayout at (drawPosX, drawPosY).
                layout.draw(g, drawPosX, drawPosY);
                // Move y-coordinate in preparation for next layout.
                drawPosY += layout.getDescent() + layout.getLeading();
            }
            table.setRowHeight(rowIndex, (int) drawPosY);
        }
    }
}
