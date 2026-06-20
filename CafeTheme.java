import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Centered theme configuration utility class for the calm Cafe vibe.
 * Defines colors, fonts, scrollbars, flat tables, and custom rounded components.
 * This class provides uniform styles used across the Java Cafe POS application.
 */
public class CafeTheme {
    
    /** The main background color for panels, dialogs, and scroll viewports (soft off-white). */
    public static final Color OFF_WHITE = new Color(253, 251, 247);
    
    /** An alternate light cream background color used for container headers and list panels. */
    public static final Color CREAM_LIGHT = new Color(248, 244, 237);
    
    /** A dark cream color used for borders, grid lines, and secondary card highlights. */
    public static final Color CREAM_DARK = new Color(238, 230, 219);
    
    /** The primary caramel color used for key action buttons, highlighting, and active states. */
    public static final Color CARAMEL = new Color(196, 114, 53);
    
    /** A very light caramel shade used as the background highlight for selected table rows. */
    public static final Color CARAMEL_LIGHT = new Color(248, 232, 216);
    
    /** A soft secondary orange accent color used as the hover state background for caramel buttons. */
    public static final Color ORANGE_SOFT = new Color(230, 137, 72);
    
    /** A dark roasted espresso brown color used for primary body text, titles, and labels. */
    public static final Color DARK_ROAST = new Color(69, 45, 34);
    
    /** A muted coffee-gray color used for secondary text, descriptions, and labels. */
    public static final Color TEXT_MUTED = new Color(143, 122, 109);
    
    /** A calm olive-green color representing success, used for checkout actions and metric cards. */
    public static final Color SUCCESS_OLIVE = new Color(101, 133, 99);
    
    /** A warm terracotta red color indicating caution/danger, used for delete actions and cancellations. */
    public static final Color DANGER_TERRACOTTA = new Color(211, 108, 88);

    /** Font for main titles and major headings (SansSerif Bold, 22pt). */
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    
    /** Font for subtitles and secondary headings (SansSerif Bold, 16pt). */
    public static final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    
    /** Font for bold labels, button text, and table headers (SansSerif Bold, 14pt). */
    public static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 14);
    
    /** Regular font for normal text input fields, table content, and general labels (SansSerif Plain, 14pt). */
    public static final Font REGULAR_FONT = new Font("SansSerif", Font.PLAIN, 14);
    
    /** Small font for badges, minor descriptions, and fine print (SansSerif Plain, 12pt). */
    public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 12);

    /**
     * Configures Swing UIManager values globally to match the Calm Cafe theme.
     * Overrides component properties like background, foreground, caret color, and fonts.
     */
    public static void applyTheme() {
        try {
            // General Components styling
            UIManager.put("Panel.background", OFF_WHITE);
            UIManager.put("OptionPane.background", OFF_WHITE);
            UIManager.put("OptionPane.messageForeground", DARK_ROAST);
            UIManager.put("OptionPane.messageFont", REGULAR_FONT);
            UIManager.put("Label.foreground", DARK_ROAST);
            UIManager.put("Label.font", REGULAR_FONT);
            
            // JTabbedPane Look & Feel properties configuration
            UIManager.put("TabbedPane.background", CREAM_LIGHT);
            UIManager.put("TabbedPane.foreground", TEXT_MUTED);
            UIManager.put("TabbedPane.selected", CARAMEL);
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
            UIManager.put("TabbedPane.font", BOLD_FONT);
            UIManager.put("TabbedPane.contentAreaColor", OFF_WHITE);
            UIManager.put("TabbedPane.shadow", CREAM_DARK);
            UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0)); // Disable the default focus dotted line
            
            // JTable default skin configuration
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.foreground", DARK_ROAST);
            UIManager.put("Table.gridColor", CREAM_DARK);
            UIManager.put("Table.selectionBackground", CARAMEL_LIGHT);
            UIManager.put("Table.selectionForeground", DARK_ROAST);
            UIManager.put("Table.font", REGULAR_FONT);
            
            UIManager.put("TableHeader.background", CREAM_LIGHT);
            UIManager.put("TableHeader.foreground", DARK_ROAST);
            UIManager.put("TableHeader.font", BOLD_FONT);
            
            // JScrollPane base setting
            UIManager.put("ScrollPane.background", OFF_WHITE);
            
            // Input elements styling
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", DARK_ROAST);
            UIManager.put("TextField.caretColor", CARAMEL);
            UIManager.put("TextField.font", REGULAR_FONT);
            
            UIManager.put("TextArea.background", Color.WHITE);
            UIManager.put("TextArea.foreground", DARK_ROAST);
            UIManager.put("TextArea.font", REGULAR_FONT);
            
            UIManager.put("ComboBox.background", Color.WHITE);
            UIManager.put("ComboBox.foreground", DARK_ROAST);
            UIManager.put("ComboBox.font", REGULAR_FONT);
            
            UIManager.put("Spinner.background", Color.WHITE);
            UIManager.put("Spinner.foreground", DARK_ROAST);
            UIManager.put("Spinner.font", REGULAR_FONT);
            
        } catch (Exception e) {
            // Ignore any configuration failures and fall back gracefully to platform defaults
        }
    }

    /**
     * Styles an existing JTable with flat designs, padded headers, and soft colors.
     * Configures row heights, gridlines, selection colors, and custom alignment renderers.
     *
     * @param table the JTable instance to style
     */
    public static void styleTable(JTable table) {
        // Set row padding and hide vertical borders for a clean flat aesthetic
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(CREAM_DARK);
        table.setSelectionBackground(CARAMEL_LIGHT);
        table.setSelectionForeground(DARK_ROAST);
        table.setFont(REGULAR_FONT);
        table.setFocusable(false);
        
        // Customize the table header
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBackground(CREAM_LIGHT);
        header.setForeground(DARK_ROAST);
        header.setFont(BOLD_FONT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CARAMEL));

        // Center align table headers using a custom delegate renderer
        header.setDefaultRenderer(new TableCellRenderer() {
            private final TableCellRenderer defaultRenderer = header.getDefaultRenderer();
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int col) {
                Component comp = defaultRenderer.getTableCellRendererComponent(t, val, isSel, hasFoc, r, col);
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setHorizontalAlignment(JLabel.CENTER);
                }
                return comp;
            }
        });

        // Center align table cell values (except the Action/Delete button column)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int col) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, col);
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setHorizontalAlignment(JLabel.CENTER);
                }
                return comp;
            }
        };

        // Apply center alignment to columns.
        // Special case: If the table has 5 columns (e.g., Inventory table), skip column 4 (Delete button)
        // so we do not overwrite its custom component renderer.
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnCount() == 5 && i == 4) {
                continue;
            }
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    /**
     * Styles an existing JScrollPane to use clean flat scrollbars.
     * Applies custom scrollbar UIs, increments, borders, and color parameters.
     *
     * @param scrollPane the JScrollPane instance to style
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        // Set a thin rounded border around the scrollpane viewport
        scrollPane.setBorder(BorderFactory.createLineBorder(CREAM_DARK, 1, true));
        
        // Set custom flat scrollbar renderers
        scrollPane.getVerticalScrollBar().setUI(new CafeScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new CafeScrollBarUI());
        
        // Define thin scrollbar widths
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        
        scrollPane.getVerticalScrollBar().setBackground(OFF_WHITE);
        scrollPane.getHorizontalScrollBar().setBackground(OFF_WHITE);
        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getHorizontalScrollBar().setOpaque(false);
        
        // Increase scroll speed for better user experience
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        scrollPane.setBackground(OFF_WHITE);
        scrollPane.getViewport().setBackground(OFF_WHITE);
    }

    /**
     * Creates an elegant border containing a rounded rectangle outline and styled title text.
     * Wraps the border in empty padding to prevent layout items from touching the borders.
     *
     * @param title the title string to display on the border
     * @return the styled CompoundBorder instance
     */
    public static Border createCafeTitledBorder(String title) {
        Border roundedBorder = BorderFactory.createLineBorder(CREAM_DARK, 1, true);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            roundedBorder, 
            "  " + title + "  ", 
            TitledBorder.LEADING, 
            TitledBorder.TOP, 
            BOLD_FONT, 
            DARK_ROAST
        );
        return BorderFactory.createCompoundBorder(
            titledBorder,
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );
    }

    /**
     * Custom JButton with rounded corners, solid background, hand cursor, and hover effects.
     * Supports multiple visual variants: PRIMARY, SECONDARY, SUCCESS, and DANGER.
     */
    public static class CafeButton extends JButton {
        
        /** Enum representing the visual styling variants of the button. */
        public enum Variant { 
            /** Caramel background with white text, representing primary actions. */
            PRIMARY, 
            /** Light cream background with dark text and border, representing secondary options. */
            SECONDARY, 
            /** Olive green background with white text, representing success states. */
            SUCCESS, 
            /** Terracotta red background with white text, representing deletion/cancellation. */
            DANGER 
        }
        
        private final Variant variant;
        private Color currentBg;
        private final Color normalBg;
        private final Color hoverBg;
        private final Color textCol;
        private int cornerRadius = 12;

        /**
         * Constructs a new CafeButton with text and style variant.
         *
         * @param text    the text to show on the button
         * @param variant the design variant to apply
         */
        public CafeButton(String text, Variant variant) {
            super(text);
            this.variant = variant;
            
            // Disable standard drawing elements for custom paint implementation
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(BOLD_FONT);

            // Assign color palettes based on the selected variant
            switch (variant) {
                case PRIMARY:
                    normalBg = CARAMEL;
                    hoverBg = ORANGE_SOFT;
                    textCol = Color.WHITE;
                    break;
                case SUCCESS:
                    normalBg = SUCCESS_OLIVE;
                    hoverBg = new Color(116, 148, 114);
                    textCol = Color.WHITE;
                    break;
                case DANGER:
                    normalBg = DANGER_TERRACOTTA;
                    hoverBg = new Color(223, 122, 103);
                    textCol = Color.WHITE;
                    break;
                case SECONDARY:
                default:
                    normalBg = CREAM_LIGHT;
                    hoverBg = CREAM_DARK;
                    textCol = DARK_ROAST;
                    break;
            }
            
            currentBg = normalBg;
            setForeground(textCol);
            setBackground(normalBg);

            // Handle hover state color transitions
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    currentBg = hoverBg;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    currentBg = normalBg;
                    repaint();
                }
            });
        }

        /**
         * Sets the corner radius of the button background shape.
         *
         * @param radius the corner radius in pixels
         */
        public void setCornerRadius(int radius) {
            this.cornerRadius = radius;
            repaint();
        }

        /**
         * Performs custom painting of the button background, outline, and text.
         * Leverages anti-aliasing rendering hints for smooth curves.
         *
         * @param g the Graphics object
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Enable rendering anti-aliasing for smooth rounded corners
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Paint the rounded button base
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

            // Paint the thin border outline for secondary buttons
            if (variant == Variant.SECONDARY) {
                g2.setColor(CREAM_DARK);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);
            }

            // Draw button text centered vertically and horizontally
            g2.setColor(textCol);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textWidth = fm.stringWidth(getText());
            int textHeight = fm.getAscent() + fm.getDescent();
            int x = (getWidth() - textWidth) / 2;
            // Center the font baseline vertically
            int y = (getHeight() - textHeight) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    /**
     * Custom UI implementation to render flat, rounded scrollbars.
     * Removes the standard arrow buttons and renders sleek rounded scrollbar tracks and thumbs.
     */
    private static class CafeScrollBarUI extends BasicScrollBarUI {

        /**
         * Replaces the decrease button with a zero-sized component to hide it.
         */
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        /**
         * Replaces the increase button with a zero-sized component to hide it.
         */
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        /**
         * Creates an invisible button of zero dimensions.
         *
         * @return a dummy JButton component
         */
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        /**
         * Paints the background track of the scrollbar.
         * Renders a soft cream-colored rounded shape.
         */
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(CREAM_LIGHT);
            g2.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, 8, 8);
            g2.dispose();
        }

        /**
         * Paints the draggable scrollbar thumb.
         * Dynamically styles based on dragging state and scroll direction (vertical vs. horizontal).
         */
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Thumb turns caramel color when actively dragged, otherwise blends with CREAM_DARK
            g2.setColor(isDragging ? CARAMEL : CREAM_DARK);
            
            // Adjust coordinates and bounds to draw a thin centered pill within the scroll track
            int w = scrollbar.getOrientation() == JScrollBar.VERTICAL ? 8 : thumbBounds.width;
            int h = scrollbar.getOrientation() == JScrollBar.VERTICAL ? thumbBounds.height : 8;
            int x = thumbBounds.x + (thumbBounds.width - w) / 2;
            int y = thumbBounds.y + (thumbBounds.height - h) / 2;
            g2.fillRoundRect(x, y, w, h, 8, 8);
            
            g2.dispose();
        }
    }
}
