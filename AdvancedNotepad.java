import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

public class AdvancedNotepad extends JFrame {

    private JTextPane textPane;
    private JTextArea lineNumbers;
    private JLabel statusLabel;
    private JLabel fileLabel;
    private JLabel appTitleLabel;
    private JLabel appSubtitleLabel;

    private final JFileChooser chooser = new JFileChooser();
    private File currentFile;
    private boolean modified = false;
    private boolean internalChange = false;

    private final UndoManager undoManager = new UndoManager();

    private int fontSize = 18;
    private String fontFamily = "Consolas";
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean wordWrap = true;
    private boolean showLineNumbers = true;

    private String theme = "Light";
    private String language = "English";

    private JPanel headerPanel;
    private JPanel topBar;
    private JPanel centerToolbarPanel;
    private JPanel toolbarShellPanel;
    private JPanel rightPanel;
    private JPanel statusPanel;
    private JPanel contentPanel;
    private JPanel editorCardPanel;
    private JScrollPane scrollPane;

    private JButton newBtn;
    private JButton openBtn;
    private JButton saveBtn;
    private JButton undoBtn;
    private JButton redoBtn;
    private JButton findBtn;
    private JButton replaceBtn;
    private JButton wrapBtn;
    private JButton dateBtn;
    private JButton settingsBtn;

    private JButton boldBtn;
    private JButton italicBtn;
    private JButton underlineBtn;

    private JComboBox<String> fontFamilyCombo;
    private JComboBox<Integer> fontSizeCombo;

    private JButton fileMenuButton;
    private JButton editMenuButton;
    private JButton viewMenuButton;
    private JButton helpMenuButton;

    private MutableAttributeSet typingAttributes;

    private static final String[] FONT_OPTIONS = {
            "Consolas", "Cascadia Code", "Courier New", "Lucida Console",
            "Arial", "Calibri", "Cambria", "Candara",
            "Corbel", "Segoe UI", "Tahoma", "Trebuchet MS",
            "Verdana", "Georgia", "Garamond", "Book Antiqua",
            "Palatino Linotype", "Times New Roman", "Franklin Gothic Medium", "Century Gothic",
            "Comic Sans MS", "Impact", "Microsoft YaHei UI", "SimSun"
    };

    private static final Integer[] FONT_SIZES = {
            8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24, 26, 28, 32, 36, 40, 48, 56, 64, 72
    };

    // Starts the program and opens the notepad window.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdvancedNotepad::new);
    }

    // Runs the setup steps that build and show the full editor.
    public AdvancedNotepad() {
        initializeFrame();
        initializeComponents();
        buildUI();
        initializeTypingAttributes();
        setupListeners();
        applyTheme();
        applyLanguage();
        updateLineNumbers();
        updateStatus();
        updateTitle();
        setVisible(true);
    }

    // Sets the main window size, title, and close settings.
    private void initializeFrame() {
        setTitle("Advanced Notepad");
        setSize(1250, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(950, 650));
    }

    // Creates the editor, buttons, labels, combo boxes, and scroll pane.
    private void initializeComponents() {
        textPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return wordWrap || super.getScrollableTracksViewportWidth();
            }
        };

        textPane.setMargin(new Insets(20, 22, 20, 22));
        textPane.setCaretColor(Color.BLACK);

        if (wordWrap) {
            textPane.setEditorKit(new WrapEditorKit());
        }

        lineNumbers = new JTextArea("1");
        lineNumbers.setEditable(false);
        lineNumbers.setFocusable(false);
        lineNumbers.setMargin(new Insets(20, 12, 20, 12));
        lineNumbers.setFont(new Font("Consolas", Font.PLAIN, 15));

        scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setRowHeaderView(lineNumbers);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        statusLabel = new JLabel("Ready");
        fileLabel = new JLabel("Untitled");
        appTitleLabel = new JLabel("Advanced Notepad");
        appSubtitleLabel = new JLabel("Focused writing with modern controls");

        headerPanel = new JPanel(new BorderLayout(10, 0));
        topBar = new JPanel(new BorderLayout(10, 0));
        centerToolbarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        toolbarShellPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        statusPanel = new JPanel(new BorderLayout());
        contentPanel = new JPanel(new BorderLayout());
        editorCardPanel = new JPanel(new BorderLayout());

        fileMenuButton = createTopMenuButton("File");
        editMenuButton = createTopMenuButton("Edit");
        viewMenuButton = createTopMenuButton("View");
        helpMenuButton = createTopMenuButton("Help");

        newBtn = createToolbarTextButton("New", "New");
        openBtn = createToolbarTextButton("Open", "Open");
        saveBtn = createToolbarTextButton("Save", "Save");
        undoBtn = createToolbarTextButton("Undo", "Undo");
        redoBtn = createToolbarTextButton("Redo", "Redo");
        findBtn = createToolbarTextButton("Find", "Find");
        replaceBtn = createToolbarTextButton("Replace", "Replace");
        wrapBtn = createToolbarTextButton("Wrap", "Word Wrap");
        dateBtn = createToolbarTextButton("Date", "Insert Date/Time");

        boldBtn = createFormatButton("B", "Bold", Font.BOLD);
        italicBtn = createFormatButton("I", "Italic", Font.ITALIC);
        underlineBtn = createFormatButton("U", "Underline", Font.PLAIN);

        fontFamilyCombo = new JComboBox<>(FONT_OPTIONS);
        fontFamilyCombo.setSelectedItem(fontFamily);
        fontFamilyCombo.setPreferredSize(new Dimension(170, 38));
        fontFamilyCombo.setFont(uiFont(Font.PLAIN, 14));

        fontSizeCombo = new JComboBox<>(FONT_SIZES);
        fontSizeCombo.setSelectedItem(fontSize);
        fontSizeCombo.setPreferredSize(new Dimension(90, 38));
        fontSizeCombo.setFont(uiFont(Font.PLAIN, 14));

        settingsBtn = createToolbarTextButton("Settings", "Settings");
        settingsBtn.setPreferredSize(new Dimension(130, 38));
        settingsBtn.setIcon(new GearIcon(16, new Color(255, 255, 255)));
        settingsBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        settingsBtn.setIconTextGap(8);
    }

    // Prepares the default style used when the user types new text.
    private void initializeTypingAttributes() {
        typingAttributes = new SimpleAttributeSet();
        applyTypingAttributesToSet();
        textPane.setCharacterAttributes(typingAttributes, false);

        installDocumentListeners(textPane.getDocument());
    }

    // Listens for text changes so the app can update title, status, and undo history.
    private void installDocumentListeners(Document doc) {
        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onDocumentChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onDocumentChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onDocumentChanged();
            }
        });

        doc.addUndoableEditListener(e -> {
            if (!internalChange) {
                undoManager.addEdit(e.getEdit());
            }
        });
    }

    // Marks the file as changed and refreshes the editor information.
    private void onDocumentChanged() {
        if (!internalChange) {
            modified = true;
            updateTitle();
            updateLineNumbers();
            updateStatus();
        }
    }

    // Copies the current font and style settings into the typing attributes.
    private void applyTypingAttributesToSet() {
        StyleConstants.setFontFamily(typingAttributes, fontFamily);
        StyleConstants.setFontSize(typingAttributes, fontSize);
        StyleConstants.setBold(typingAttributes, bold);
        StyleConstants.setItalic(typingAttributes, italic);
        StyleConstants.setUnderline(typingAttributes, underline);
    }

    private class WrapEditorKit extends StyledEditorKit {
        private final ViewFactory factory = new WrapColumnFactory();

        @Override
        public ViewFactory getViewFactory() {
            return factory;
        }
    }

    private static class WrapColumnFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new WrapLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }

    private static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            if (axis == View.X_AXIS) {
                return 0;
            }
            return super.getMinimumSpan(axis);
        }
    }

    // Builds the full window layout: top area, editor area, and status bar.
    private void buildUI() {
        setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        appTitleLabel.setFont(titleFont(Font.BOLD, 30));
        appSubtitleLabel.setFont(uiFont(Font.PLAIN, 13));
        titlePanel.add(appTitleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(appSubtitleLabel);

        JPanel leftMenus = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftMenus.setOpaque(false);
        leftMenus.add(fileMenuButton);
        leftMenus.add(editMenuButton);
        leftMenus.add(viewMenuButton);
        leftMenus.add(helpMenuButton);

        JPanel leftCluster = new JPanel(new BorderLayout(0, 10));
        leftCluster.setOpaque(false);
        leftCluster.add(titlePanel, BorderLayout.NORTH);
        leftCluster.add(leftMenus, BorderLayout.SOUTH);

        centerToolbarPanel.setOpaque(false);
        centerToolbarPanel.add(fontSizeCombo);
        centerToolbarPanel.add(createDivider());

        centerToolbarPanel.add(fontFamilyCombo);
        centerToolbarPanel.add(createDivider());

        centerToolbarPanel.add(boldBtn);
        centerToolbarPanel.add(italicBtn);
        centerToolbarPanel.add(underlineBtn);
        centerToolbarPanel.add(createDivider());

        centerToolbarPanel.add(newBtn);
        centerToolbarPanel.add(openBtn);
        centerToolbarPanel.add(saveBtn);
        centerToolbarPanel.add(createDivider());
        centerToolbarPanel.add(replaceBtn);

        toolbarShellPanel.setOpaque(true);
        toolbarShellPanel.add(centerToolbarPanel);

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setOpaque(false);
        centerWrapper.add(toolbarShellPanel);

        rightPanel.setOpaque(false);
        rightPanel.add(settingsBtn);

        topBar.setOpaque(true);
        topBar.setBorder(new EmptyBorder(18, 22, 18, 22));
        topBar.add(leftCluster, BorderLayout.WEST);
        topBar.add(centerWrapper, BorderLayout.CENTER);
        topBar.add(rightPanel, BorderLayout.EAST);

        headerPanel.setBorder(new EmptyBorder(0, 18, 12, 18));
        headerPanel.setOpaque(false);
        headerPanel.add(topBar, BorderLayout.CENTER);

        editorCardPanel.setBorder(new EmptyBorder(0, 24, 0, 24));
        editorCardPanel.setOpaque(true);
        editorCardPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.setBorder(new EmptyBorder(0, 0, 14, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(editorCardPanel, BorderLayout.CENTER);

        statusPanel.setBorder(new EmptyBorder(12, 26, 20, 26));
        statusPanel.setOpaque(false);
        statusPanel.add(fileLabel, BorderLayout.WEST);
        statusPanel.add(statusLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    // Creates a styled menu button for the top bar.
    private JButton createTopMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(uiFont(Font.BOLD, 14));
        btn.setFocusPainted(false);
        return btn;
    }

    // Creates a normal toolbar button with shared styling.
    private JButton createToolbarTextButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(96, 38));
        btn.setFont(uiFont(Font.BOLD, 13));
        styleToolbarButton(btn);
        return btn;
    }

    // Creates a small toolbar button for bold, italic, or underline.
    private JButton createFormatButton(String text, String tooltip, int fontStyle) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(46, 38));
        btn.setFont(uiFont(fontStyle, 16));
        styleToolbarButton(btn);
        return btn;
    }

    // Creates a small label used inside the toolbar.
    private JLabel createMiniToolbarLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(uiFont(Font.BOLD, 12));
        label.setBorder(new EmptyBorder(0, 4, 0, 0));
        return label;
    }

    // Creates a divider line between toolbar groups.
    private JComponent createDivider() {
        JPanel divider = new JPanel();
        divider.setOpaque(true);
        divider.setPreferredSize(new Dimension(1, 22));
        divider.setBorder(new EmptyBorder(0, 8, 0, 8));
        return divider;
    }

    // Applies the common look used by toolbar buttons.
    private void styleToolbarButton(JButton btn) {
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(4, 10, 4, 10));
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(205, 210, 218), 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
    }

    // Applies a flat color style to a button.
    private void styleFlatButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
    }

    // Styles combo boxes to match the custom notepad theme.
    private void styleComboBox(JComponent component, Color bg, Color fg, Color border) {
        component.setBackground(bg);
        component.setForeground(fg);
        component.setBorder(new CompoundBorder(
                new LineBorder(border, 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));
        if (component instanceof JComboBox) {
            ((JComboBox<?>) component).setFocusable(false);
        }
    }

    // Chooses the best UI font family for the current language.
    private String getUIFontFamily() {
        if ("中文".equals(language)) {
            return "Microsoft YaHei UI";
        }
        if ("العربية".equals(language)) {
            return "Segoe UI";
        }
        return "Segoe UI";
    }

    // Chooses the title font family for the current language.
    private String getTitleFontFamily() {
        if ("中文".equals(language)) {
            return "Microsoft YaHei UI";
        }
        if ("العربية".equals(language)) {
            return "Segoe UI";
        }
        return "Georgia";
    }

    // Creates a normal UI font using the selected UI font family.
    private Font uiFont(int style, int size) {
        return new Font(getUIFontFamily(), style, size);
    }

    // Creates a title font using the selected title font family.
    private Font titleFont(int style, int size) {
        return new Font(getTitleFontFamily(), style, size);
    }

    // Picks a safe preview font for one language name in the language dropdown.
    private Font getSampleLanguageFont(String languageName, int style, int size) {
        if ("中文".equals(languageName)) {
            return new Font("Microsoft YaHei UI", style, size);
        }
        if ("العربية".equals(languageName)) {
            return new Font("Segoe UI", style, size);
        }
        return new Font("Segoe UI", style, size);
    }

    // Returns the colors used by the current theme.
    private Color[] getThemePalette() {
        boolean dark = theme.equalsIgnoreCase("Dark");
        Color bg = dark ? new Color(20, 24, 32) : new Color(243, 238, 229);
        Color panel = dark ? new Color(30, 36, 48) : new Color(234, 226, 213);
        Color editor = dark ? new Color(14, 18, 26) : new Color(255, 252, 246);
        Color fg = dark ? new Color(238, 239, 243) : new Color(48, 39, 30);
        Color sub = dark ? new Color(155, 166, 184) : new Color(121, 105, 88);
        Color border = dark ? new Color(61, 72, 92) : new Color(206, 193, 175);
        Color cardBorder = dark ? new Color(55, 67, 88) : new Color(198, 184, 163);
        Color buttonBg = dark ? new Color(38, 47, 63) : new Color(249, 245, 237);
        Color accent = dark ? new Color(95, 140, 224) : new Color(173, 92, 38);
        Color accentSoft = dark ? new Color(55, 79, 122) : new Color(233, 212, 190);
        return new Color[]{bg, panel, editor, fg, sub, border, cardBorder, buttonBg, accent, accentSoft};
    }

    // Creates one styled popup menu item and its action.
    private JMenuItem createStyledMenuItem(String text, ActionListener action, KeyStroke keyStroke) {
        Color[] palette = getThemePalette();
        Color editor = palette[2];
        Color fg = palette[3];
        Color sub = palette[4];

        JMenuItem item = new JMenuItem(text);
        item.addActionListener(action);
        if (keyStroke != null) {
            item.setAccelerator(keyStroke);
        }
        item.setOpaque(true);
        item.setBackground(editor);
        item.setForeground(fg);
        item.setFont(uiFont(Font.BOLD, 14));
        item.setBorder(new EmptyBorder(10, 14, 10, 14));
        item.setIconTextGap(10);
        item.setHorizontalTextPosition(SwingConstants.LEFT);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setArmed(false);
        return item;
    }

    // Styles popup menus like the right-click menu and top menus.
    private void stylePopupMenu(JPopupMenu menu) {
        Color[] palette = getThemePalette();
        Color editor = palette[2];
        Color border = palette[5];

        menu.setOpaque(true);
        menu.setBackground(editor);
        menu.setBorder(new CompoundBorder(
                new LineBorder(border, 1, true),
                new EmptyBorder(6, 6, 6, 6)
        ));
    }

    // Creates a separator line for popup menus.
    private JSeparator createStyledSeparator() {
        Color[] palette = getThemePalette();
        JSeparator separator = new JSeparator();
        separator.setForeground(palette[5]);
        separator.setBackground(palette[5]);
        return separator;
    }

    // Creates a card section used inside the settings dialog.
    private JPanel createSettingsCard(Color background, Color border, int top, int left, int bottom, int right) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(background);
        panel.setBorder(new CompoundBorder(
                new LineBorder(border, 1, true),
                new EmptyBorder(top, left, bottom, right)
        ));
        return panel;
    }

    // Styles a checkbox so it matches the notepad design.
    private void styleSettingsCheckBox(JCheckBox checkBox, Color bg, Color fg) {
        checkBox.setOpaque(false);
        checkBox.setForeground(fg);
        checkBox.setFont(uiFont(Font.BOLD, 14));
        checkBox.setFocusPainted(false);
        checkBox.setBorder(new EmptyBorder(4, 2, 4, 2));
        checkBox.setIcon(new CheckBoxIcon(bg.darker(), new Color(255, 255, 255, 0), fg));
        checkBox.setSelectedIcon(new CheckBoxIcon(bg.darker(), getThemePalette()[8], Color.WHITE));
    }

    // Styles the language dropdown and makes each language show with a readable font.
    private void styleLanguageComboBox(JComboBox<String> comboBox, Color bg, Color fg, Color border) {
        styleComboBox(comboBox, bg, fg, border);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String text = value == null ? "" : value.toString();
                label.setFont(getSampleLanguageFont(text, Font.PLAIN, 14));
                label.setBorder(new EmptyBorder(6, 8, 6, 8));
                return label;
            }
        });
    }

    // Connects buttons, menus, and editor events to their actions.
    private void setupListeners() {
        newBtn.addActionListener(e -> newFile());
        openBtn.addActionListener(e -> openFile());
        saveBtn.addActionListener(e -> saveFile());
        undoBtn.addActionListener(e -> undo());
        redoBtn.addActionListener(e -> redo());
        findBtn.addActionListener(e -> openFindDialog());
        replaceBtn.addActionListener(e -> openReplaceDialog());
        wrapBtn.addActionListener(e -> toggleWordWrap());
        dateBtn.addActionListener(e -> insertDateTime());
        settingsBtn.addActionListener(e -> openSettingsDialog());

        boldBtn.addActionListener(e -> toggleBold());
        italicBtn.addActionListener(e -> toggleItalic());
        underlineBtn.addActionListener(e -> toggleUnderline());

        fileMenuButton.addActionListener(e -> buildFileMenu().show(fileMenuButton, 0, fileMenuButton.getHeight()));
        editMenuButton.addActionListener(e -> buildEditMenu().show(editMenuButton, 0, editMenuButton.getHeight()));
        viewMenuButton.addActionListener(e -> buildViewMenu().show(viewMenuButton, 0, viewMenuButton.getHeight()));
        helpMenuButton.addActionListener(e -> buildHelpMenu().show(helpMenuButton, 0, helpMenuButton.getHeight()));

        fontFamilyCombo.addActionListener(e -> {
            Object selected = fontFamilyCombo.getSelectedItem();
            if (selected != null) {
                fontFamily = selected.toString();
                applyFontFamilyOrSizeToSelectionOrTypingState();
            }
        });

        fontSizeCombo.addActionListener(e -> {
            Object selected = fontSizeCombo.getSelectedItem();
            if (selected instanceof Integer) {
                fontSize = (Integer) selected;
                applyFontFamilyOrSizeToSelectionOrTypingState();
            }
        });

        textPane.addCaretListener(e -> {
            syncFormattingStateFromCaretOrSelection();
            updateStatus();
        });

        textPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                zoom(e.getWheelRotation() < 0 ? 2 : -2);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tryExit();
            }
        });

        createRightClickMenu();
        setupShortcuts();
    }

    // Adds keyboard shortcuts like Ctrl+S, Ctrl+F, and Ctrl+B.
    private void setupShortcuts() {
        InputMap im = textPane.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = textPane.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "newFile");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "openFile");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "saveFile");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "find");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK), "replace");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK), "bold");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK), "italic");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK), "underline");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "dateTime");

        am.put("newFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFile();
            }
        });
        am.put("openFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });
        am.put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });
        am.put("find", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFindDialog();
            }
        });
        am.put("replace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openReplaceDialog();
            }
        });
        am.put("bold", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleBold();
            }
        });
        am.put("italic", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleItalic();
            }
        });
        am.put("underline", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleUnderline();
            }
        });
        am.put("dateTime", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertDateTime();
            }
        });
    }

    // Builds the File menu and its items.
    private JPopupMenu buildFileMenu() {
        JPopupMenu menu = new JPopupMenu();
        stylePopupMenu(menu);
        addMenuItem(menu, t("New"), e -> newFile(), KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Open"), e -> openFile(), KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Save"), e -> saveFile(), KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Save As"), e -> saveFileAs(), KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        menu.add(createStyledSeparator());
        addMenuItem(menu, t("Exit"), e -> tryExit(), null);
        return menu;
    }

    // Builds the Edit menu and its items.
    private JPopupMenu buildEditMenu() {
        JPopupMenu menu = new JPopupMenu();
        stylePopupMenu(menu);
        addMenuItem(menu, t("Undo"), e -> undo(), KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Redo"), e -> redo(), KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        menu.add(createStyledSeparator());
        addMenuItem(menu, t("Cut"), e -> textPane.cut(), KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Copy"), e -> textPane.copy(), KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Paste"), e -> textPane.paste(), KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Select All"), e -> textPane.selectAll(), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        menu.add(createStyledSeparator());
        addMenuItem(menu, t("Find"), e -> openFindDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Replace"), e -> openReplaceDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Insert Date/Time"), e -> insertDateTime(), KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        return menu;
    }

    // Builds the View menu and its items.
    private JPopupMenu buildViewMenu() {
        JPopupMenu menu = new JPopupMenu();
        stylePopupMenu(menu);
        addMenuItem(menu, t("Zoom In"), e -> zoom(2), KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Zoom Out"), e -> zoom(-2), KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Reset Zoom"), e -> resetZoom(), KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
        menu.add(createStyledSeparator());
        addMenuItem(menu, t("Toggle Bold"), e -> toggleBold(), KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Toggle Italic"), e -> toggleItalic(), KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Toggle Underline"), e -> toggleUnderline(), KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
        addMenuItem(menu, t("Toggle Word Wrap"), e -> toggleWordWrap(), null);
        addMenuItem(menu, t("Toggle Line Numbers"), e -> toggleLineNumbers(), null);
        menu.add(createStyledSeparator());
        addMenuItem(menu, t("Settings"), e -> openSettingsDialog(), null);
        return menu;
    }

    // Builds the Help menu.
    private JPopupMenu buildHelpMenu() {
        JPopupMenu menu = new JPopupMenu();
        stylePopupMenu(menu);
        addMenuItem(menu, t("About"), e -> showAbout(), null);
        return menu;
    }

    // Adds one styled menu item into a popup menu.
    private void addMenuItem(JPopupMenu menu, String text, ActionListener action, KeyStroke keyStroke) {
        menu.add(createStyledMenuItem(text, action, keyStroke));
    }

    // Attaches the custom right-click menu to the text editor.
    private void createRightClickMenu() {
        textPane.setComponentPopupMenu(buildRightClickMenu());
    }

    // Builds the menu that appears when the user right-clicks in the editor.
    private JPopupMenu buildRightClickMenu() {
        JPopupMenu menu = new JPopupMenu();
        stylePopupMenu(menu);
        menu.add(createStyledMenuItem(t("Cut"), e -> textPane.cut(), KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK)));
        menu.add(createStyledMenuItem(t("Copy"), e -> textPane.copy(), KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK)));
        menu.add(createStyledMenuItem(t("Paste"), e -> textPane.paste(), KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK)));
        menu.add(createStyledSeparator());
        menu.add(createStyledMenuItem(t("Find"), e -> openFindDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK)));
        menu.add(createStyledMenuItem(t("Replace"), e -> openReplaceDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK)));
        menu.add(createStyledSeparator());
        menu.add(createStyledMenuItem(t("Select All"), e -> textPane.selectAll(), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK)));
        return menu;
    }

    // Clears the editor and starts a new blank document.
    private void newFile() {
        if (!confirmSaveIfNeeded()) {
            return;
        }

        internalChange = true;
        textPane.setText("");
        internalChange = false;

        currentFile = null;
        modified = false;
        undoManager.discardAllEdits();

        resetTypingState();
        updateTitle();
        updateLineNumbers();
        updateStatus();
    }

    // Opens a file and loads its text into the editor.
    private void openFile() {
        if (!confirmSaveIfNeeded()) {
            return;
        }

        if (showStyledFileChooser(false) == JFileChooser.APPROVE_OPTION) {
            try {
                currentFile = chooser.getSelectedFile();
                String content = Files.readString(currentFile.toPath(), StandardCharsets.UTF_8);

                internalChange = true;
                textPane.setText(content);
                textPane.setCaretPosition(0);
                internalChange = false;

                modified = false;
                undoManager.discardAllEdits();
                resetTypingState();
                updateTitle();
                updateLineNumbers();
                updateStatusMessage(t("File opened successfully"));
                updateStatus();
            } catch (Exception ex) {
                showError("Could not open file:\n" + ex.getMessage());
            }
        }
    }

    // Saves the current document to disk.
    private void saveFile() {
        try {
            if (currentFile == null) {
                if (showStyledFileChooser(true) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                currentFile = chooser.getSelectedFile();
            }

            Files.writeString(currentFile.toPath(), textPane.getText(), StandardCharsets.UTF_8);
            modified = false;
            updateTitle();
            updateStatusMessage(t("File saved successfully"));
            updateStatus();
        } catch (Exception ex) {
            showError("Could not save file:\n" + ex.getMessage());
        }
    }

    // Saves the current document using a new file path.
    private void saveFileAs() {
        try {
            if (showStyledFileChooser(true) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            currentFile = chooser.getSelectedFile();
            Files.writeString(currentFile.toPath(), textPane.getText(), StandardCharsets.UTF_8);
            modified = false;
            updateTitle();
            updateStatusMessage(t("File saved successfully"));
            updateStatus();
        } catch (Exception ex) {
            showError("Could not save file:\n" + ex.getMessage());
        }
    }

    // Tries to close the app safely after checking for unsaved changes.
    private void tryExit() {
        if (confirmSaveIfNeeded()) {
            dispose();
        }
    }

    // Shows a confirmation dialog if the document has unsaved changes.
    private boolean confirmSaveIfNeeded() {
        if (!modified) {
            return true;
        }

        int result = showStyledConfirmDialog(
                t("Confirm"),
                t("You have unsaved changes. Save before continuing?"),
                "Unsaved changes",
                "Save first to avoid losing your work."
        );

        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            return false;
        }

        if (result == JOptionPane.YES_OPTION) {
            saveFile();
            return !modified;
        }

        return true;
    }

    // Undoes the last change.
    private void undo() {
        try {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } catch (CannotUndoException ignored) {
        }
        syncFormattingStateFromCaretOrSelection();
        updateLineNumbers();
        updateStatus();
    }

    // Redoes the last undone change.
    private void redo() {
        try {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        } catch (CannotRedoException ignored) {
        }
        syncFormattingStateFromCaretOrSelection();
        updateLineNumbers();
        updateStatus();
    }

    // Changes the font size by the given amount.
    private void zoom(int value) {
        fontSize = Math.max(8, Math.min(72, fontSize + value));
        fontSizeCombo.setSelectedItem(fontSize);
        applyFontFamilyOrSizeToSelectionOrTypingState();
    }

    // Restores the editor zoom to the default size.
    private void resetZoom() {
        fontSize = 18;
        fontSizeCombo.setSelectedItem(fontSize);
        applyFontFamilyOrSizeToSelectionOrTypingState();
    }

    // Turns bold formatting on or off.
    private void toggleBold() {
        if (hasSelection()) {
            applyBooleanStyleToSelection(StyleType.BOLD);
        } else {
            bold = !bold;
            applyTypingAttributesToSet();
            textPane.setCharacterAttributes(typingAttributes, false);
            refreshFormatButtons();
        }
        updateStatusMessage(t("Bold updated"));
    }

    // Turns italic formatting on or off.
    private void toggleItalic() {
        if (hasSelection()) {
            applyBooleanStyleToSelection(StyleType.ITALIC);
        } else {
            italic = !italic;
            applyTypingAttributesToSet();
            textPane.setCharacterAttributes(typingAttributes, false);
            refreshFormatButtons();
        }
        updateStatusMessage(t("Italic updated"));
    }

    // Turns underline formatting on or off.
    private void toggleUnderline() {
        if (hasSelection()) {
            applyBooleanStyleToSelection(StyleType.UNDERLINE);
        } else {
            underline = !underline;
            applyTypingAttributesToSet();
            textPane.setCharacterAttributes(typingAttributes, false);
            refreshFormatButtons();
        }
        updateStatusMessage(t("Underline updated"));
    }

    private enum StyleType {
        BOLD, ITALIC, UNDERLINE
    }

    // Checks whether text is currently selected.
    private boolean hasSelection() {
        return textPane.getSelectionStart() != textPane.getSelectionEnd();
    }

    // Applies one text style across the selected text.
    private void applyBooleanStyleToSelection(StyleType type) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();

        if (start >= end) {
            return;
        }

        boolean allEnabled = true;

        for (int i = start; i < end; i++) {
            Element elem = doc.getCharacterElement(i);
            AttributeSet attrs = elem.getAttributes();
            boolean value;

            switch (type) {
                case BOLD:
                    value = StyleConstants.isBold(attrs);
                    break;
                case ITALIC:
                    value = StyleConstants.isItalic(attrs);
                    break;
                default:
                    value = StyleConstants.isUnderline(attrs);
                    break;
            }

            if (!value) {
                allEnabled = false;
                break;
            }
        }

        boolean newValue = !allEnabled;
        MutableAttributeSet attrs = new SimpleAttributeSet();

        switch (type) {
            case BOLD:
                StyleConstants.setBold(attrs, newValue);
                bold = newValue;
                break;
            case ITALIC:
                StyleConstants.setItalic(attrs, newValue);
                italic = newValue;
                break;
            case UNDERLINE:
                StyleConstants.setUnderline(attrs, newValue);
                underline = newValue;
                break;
        }

        doc.setCharacterAttributes(start, end - start, attrs, false);

        applyTypingAttributesToSet();
        textPane.setCharacterAttributes(typingAttributes, false);
        refreshFormatButtons();
    }

    // Applies the selected font family or font size to text and typing state.
    private void applyFontFamilyOrSizeToSelectionOrTypingState() {
        if (hasSelection()) {
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();

            MutableAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, fontFamily);
            StyleConstants.setFontSize(attrs, fontSize);
            textPane.getStyledDocument().setCharacterAttributes(start, end - start, attrs, false);
        }

        applyTypingAttributesToSet();
        textPane.setCharacterAttributes(typingAttributes, false);
        lineNumbers.setFont(new Font("Consolas", Font.PLAIN, Math.max(12, fontSize - 2)));
        updateStatus();
    }

    // Reads the current text style so the toolbar stays in sync with the caret.
    private void syncFormattingStateFromCaretOrSelection() {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();

        if (doc.getLength() == 0) {
            refreshFormatButtons();
            return;
        }

        int pos;
        if (start != end) {
            pos = start;
        } else {
            pos = Math.max(0, textPane.getCaretPosition() - 1);
        }

        if (pos >= doc.getLength()) {
            pos = doc.getLength() - 1;
        }

        if (pos < 0) {
            return;
        }

        Element elem = doc.getCharacterElement(pos);
        AttributeSet attrs = elem.getAttributes();

        bold = StyleConstants.isBold(attrs);
        italic = StyleConstants.isItalic(attrs);
        underline = StyleConstants.isUnderline(attrs);

        String family = StyleConstants.getFontFamily(attrs);
        int size = StyleConstants.getFontSize(attrs);

        if (family != null) {
            fontFamily = family;
        }
        if (size > 0) {
            fontSize = size;
        }

        fontFamilyCombo.setSelectedItem(fontFamily);
        fontSizeCombo.setSelectedItem(fontSize);

        applyTypingAttributesToSet();
        refreshFormatButtons();
    }

    // Refreshes the active look of formatting buttons.
    private void refreshFormatButtons() {
        updateToggleButton(boldBtn, bold);
        updateToggleButton(italicBtn, italic);
        updateToggleButton(underlineBtn, underline);
    }

    // Updates one format button to show whether it is active.
    private void updateToggleButton(JButton button, boolean active) {
        boolean dark = theme.equalsIgnoreCase("Dark");
        Color neutralBg = dark ? new Color(38, 47, 63) : new Color(249, 245, 237);
        Color neutralFg = dark ? new Color(230, 235, 242) : new Color(48, 39, 30);
        Color neutralBorder = dark ? new Color(61, 72, 92) : new Color(206, 193, 175);
        Color activeBg = dark ? new Color(95, 140, 224) : new Color(173, 92, 38);
        Color activeBorder = dark ? new Color(95, 140, 224) : new Color(173, 92, 38);
        if (active) {
            button.setBackground(activeBg);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(activeBorder, 1, true),
                    new EmptyBorder(7, 12, 7, 12)
            ));
        } else {
            button.setBackground(neutralBg);
            button.setForeground(neutralFg);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(neutralBorder, 1, true),
                    new EmptyBorder(7, 12, 7, 12)
            ));
        }
    }

    // Turns word wrap on or off.
    private void toggleWordWrap() {
        wordWrap = !wordWrap;
        rebuildDocumentPreservingStyles();
        updateStatusMessage(wordWrap ? t("Word wrap enabled") : t("Word wrap disabled"));
    }

    // Sets word wrap directly.
    private void setWordWrap(boolean value) {
        if (wordWrap != value) {
            wordWrap = value;
            rebuildDocumentPreservingStyles();
        }
    }

    // Rebuilds the document so wrap changes do not remove text styling.
    private void rebuildDocumentPreservingStyles() {
        String text = textPane.getText();
        StyledDocument oldDoc = textPane.getStyledDocument();
        int caret = textPane.getCaretPosition();

        DefaultStyledDocument newDoc = new DefaultStyledDocument();

        try {
            newDoc.insertString(0, text, null);

            for (int i = 0; i < oldDoc.getLength(); i++) {
                Element elem = oldDoc.getCharacterElement(i);
                AttributeSet attrs = elem.getAttributes();
                newDoc.setCharacterAttributes(i, 1, attrs, true);
            }
        } catch (BadLocationException ex) {
            showError("Could not rebuild document:\n" + ex.getMessage());
            return;
        }

        internalChange = true;
        textPane.setEditorKit(wordWrap ? new WrapEditorKit() : new StyledEditorKit());
        textPane.setDocument(newDoc);
        textPane.setCaretPosition(Math.min(caret, newDoc.getLength()));
        internalChange = false;

        installDocumentListeners(newDoc);
        textPane.setCharacterAttributes(typingAttributes, false);
        createRightClickMenu();
        updateLineNumbers();
        updateStatus();
    }

    // Shows or hides line numbers.
    private void toggleLineNumbers() {
        showLineNumbers = !showLineNumbers;
        scrollPane.setRowHeaderView(showLineNumbers ? lineNumbers : null);
        updateStatusMessage(showLineNumbers ? t("Line numbers shown") : t("Line numbers hidden"));
    }

    // Sets line number visibility directly.
    private void setLineNumbersVisible(boolean value) {
        showLineNumbers = value;
        scrollPane.setRowHeaderView(showLineNumbers ? lineNumbers : null);
    }

    // Inserts the current date and time into the editor.
    private void insertDateTime() {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        textPane.replaceSelection(time);
    }

    // Opens the Find dialog.
    private void openFindDialog() {
        JDialog dialog = new JDialog(this, t("Find"), false);
        dialog.setSize(360, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTextField field = new JTextField();
        JButton findNext = new JButton(t("Find Next"));

        panel.add(new JLabel(t("Find what:")), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.add(findNext, BorderLayout.EAST);

        findNext.addActionListener(e -> findText(field.getText()));

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // Opens the Replace dialog.
    private void openReplaceDialog() {
        JDialog dialog = new JDialog(this, t("Replace"), false);
        dialog.setSize(420, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTextField findField = new JTextField();
        JTextField replaceField = new JTextField();
        JButton replaceBtn2 = new JButton(t("Replace"));
        JButton replaceAllBtn = new JButton(t("Replace All"));

        panel.add(new JLabel(t("Find what:")));
        panel.add(findField);
        panel.add(new JLabel(t("Replace with:")));
        panel.add(replaceField);
        panel.add(replaceBtn2);
        panel.add(replaceAllBtn);

        replaceBtn2.addActionListener(e -> replaceOne(findField.getText(), replaceField.getText()));
        replaceAllBtn.addActionListener(e -> replaceAll(findField.getText(), replaceField.getText()));

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // Searches for the next matching text in the editor.
    private void findText(String target) {
        if (target == null || target.isEmpty()) {
            return;
        }

        String text = textPane.getText();
        int start = textPane.getCaretPosition();
        int index = text.indexOf(target, start);

        if (index == -1) {
            index = text.indexOf(target);
        }

        if (index >= 0) {
            textPane.requestFocus();
            textPane.select(index, index + target.length());
        } else {
            JOptionPane.showMessageDialog(this, t("Text not found"), t("Find"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Replaces the current match or moves to the next one.
    private void replaceOne(String find, String replace) {
        if (find == null || find.isEmpty()) {
            return;
        }

        String selected = textPane.getSelectedText();
        if (selected != null && selected.equals(find)) {
            textPane.replaceSelection(replace);
        } else {
            findText(find);
        }
    }

    // Replaces all matching text in the document.
    private void replaceAll(String find, String replace) {
        if (find == null || find.isEmpty()) {
            return;
        }

        internalChange = true;
        textPane.setText(textPane.getText().replace(find, replace));
        internalChange = false;

        modified = true;
        updateTitle();
        updateLineNumbers();
        updateStatus();
    }

    // Builds and shows the Settings dialog.
    private void openSettingsDialog() {
        Color[] palette = getThemePalette();
        Color bg = palette[0];
        Color panel = palette[1];
        Color editor = palette[2];
        Color fg = palette[3];
        Color sub = palette[4];
        Color border = palette[5];
        Color buttonBg = palette[7];
        Color accent = palette[8];

        JDialog dialog = new JDialog(this, t("Settings"), true);
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(bg);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(bg);
        header.setBorder(new EmptyBorder(20, 22, 12, 22));
        JLabel title = new JLabel(t("Settings"));
        title.setFont(titleFont(Font.BOLD, 26));
        title.setForeground(fg);
        JLabel subtitle = new JLabel("Personalize the editor experience");
        subtitle.setFont(uiFont(Font.PLAIN, 13));
        subtitle.setForeground(sub);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        JComboBox<String> themeCombo = new JComboBox<>(new String[]{"Light", "Dark"});
        themeCombo.setSelectedItem(theme);

        JComboBox<String> languageCombo = new JComboBox<>(new String[]{"English", "中文", "العربية"});
        languageCombo.setSelectedItem(language);

        JComboBox<String> fontCombo = new JComboBox<>(FONT_OPTIONS);
        fontCombo.setSelectedItem(fontFamily);

        JComboBox<Integer> sizeCombo = new JComboBox<>(FONT_SIZES);
        sizeCombo.setSelectedItem(fontSize);

        JCheckBox boldCheck = new JCheckBox(t("Bold text"));
        boldCheck.setSelected(bold);

        JCheckBox italicCheck = new JCheckBox(t("Italic text"));
        italicCheck.setSelected(italic);

        JCheckBox underlineCheck = new JCheckBox(t("Underline text"));
        underlineCheck.setSelected(underline);

        JCheckBox wrapCheck = new JCheckBox(t("Word wrap"));
        wrapCheck.setSelected(wordWrap);

        JCheckBox lineCheck = new JCheckBox(t("Show line numbers"));
        lineCheck.setSelected(showLineNumbers);

        styleComboBox(themeCombo, buttonBg, fg, border);
        styleLanguageComboBox(languageCombo, buttonBg, fg, border);
        styleComboBox(fontCombo, buttonBg, fg, border);
        styleComboBox(sizeCombo, buttonBg, fg, border);

        styleSettingsCheckBox(boldCheck, buttonBg, fg);
        styleSettingsCheckBox(italicCheck, buttonBg, fg);
        styleSettingsCheckBox(underlineCheck, buttonBg, fg);
        styleSettingsCheckBox(wrapCheck, buttonBg, fg);
        styleSettingsCheckBox(lineCheck, buttonBg, fg);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 22, 18, 22));

        JPanel topCard = createSettingsCard(editor, border, 18, 18, 18, 18);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        addSettingsRow(topCard, gbc, row++, t("Theme"), themeCombo, fg, sub);
        addSettingsRow(topCard, gbc, row++, t("Language"), languageCombo, fg, sub);
        addSettingsRow(topCard, gbc, row++, t("Font Family"), fontCombo, fg, sub);
        addSettingsRow(topCard, gbc, row++, t("Font Size"), sizeCombo, fg, sub);

        JPanel optionsCard = createSettingsCard(editor, border, 14, 18, 14, 18);
        optionsCard.setLayout(new GridLayout(0, 1, 0, 6));
        optionsCard.add(boldCheck);
        optionsCard.add(italicCheck);
        optionsCard.add(underlineCheck);
        optionsCard.add(wrapCheck);
        optionsCard.add(lineCheck);

        content.add(topCard, BorderLayout.NORTH);
        content.add(optionsCard, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(0, 22, 20, 22));
        JButton applyBtn = new JButton(t("Apply"));
        JButton closeBtn = new JButton(t("Close"));
        applyBtn.setFont(uiFont(Font.BOLD, 14));
        closeBtn.setFont(uiFont(Font.BOLD, 14));
        applyBtn.setPreferredSize(new Dimension(116, 40));
        closeBtn.setPreferredSize(new Dimension(116, 40));
        applyBtn.setBackground(accent);
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setBorder(new CompoundBorder(new LineBorder(accent, 1, true), new EmptyBorder(8, 14, 8, 14)));
        closeBtn.setBackground(buttonBg);
        closeBtn.setForeground(fg);
        closeBtn.setBorder(new CompoundBorder(new LineBorder(border, 1, true), new EmptyBorder(8, 14, 8, 14)));
        applyBtn.setFocusPainted(false);
        closeBtn.setFocusPainted(false);
        buttons.add(applyBtn);
        buttons.add(closeBtn);

        applyBtn.addActionListener(e -> {
            theme = String.valueOf(themeCombo.getSelectedItem());
            language = String.valueOf(languageCombo.getSelectedItem());

            fontFamily = String.valueOf(fontCombo.getSelectedItem());
            Object selectedSize = sizeCombo.getSelectedItem();
            if (selectedSize instanceof Integer) {
                fontSize = (Integer) selectedSize;
            }

            bold = boldCheck.isSelected();
            italic = italicCheck.isSelected();
            underline = underlineCheck.isSelected();

            boolean newWrap = wrapCheck.isSelected();
            boolean newLineNumbers = lineCheck.isSelected();

            fontFamilyCombo.setSelectedItem(fontFamily);
            fontSizeCombo.setSelectedItem(fontSize);

            applyTypingAttributesToSet();
            textPane.setCharacterAttributes(typingAttributes, false);
            refreshFormatButtons();

            setWordWrap(newWrap);
            setLineNumbersVisible(newLineNumbers);

            applyTheme();
            applyLanguage();
            createRightClickMenu();
            updateStatus();
            dialog.dispose();
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Adds one labeled control row to the settings form.
    private void addSettingsRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent component, Color fg, Color sub) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(uiFont(Font.BOLD, 14));
        label.setForeground(fg);
        label.setBorder(new EmptyBorder(0, 0, 0, 6));
        panel.add(label, gbc);

        gbc.gridx = 1;
        component.setFont(uiFont(Font.PLAIN, 14));
        panel.add(component, gbc);
    }

    // Styles and opens the file chooser for opening or saving files.
    private int showStyledFileChooser(boolean saveDialog) {
        Color[] palette = getThemePalette();
        Color bg = palette[0];
        Color panel = palette[1];
        Color editor = palette[2];
        Color fg = palette[3];
        Color border = palette[5];
        Color buttonBg = palette[7];
        Color accent = palette[8];

        chooser.setDialogTitle(saveDialog ? t("Save") : t("Open"));
        chooser.setApproveButtonText(saveDialog ? t("Save") : t("Open"));
        chooser.setBackground(bg);
        styleFileChooserComponentTree(chooser, bg, panel, editor, fg, border, buttonBg, accent);

        return saveDialog ? chooser.showSaveDialog(this) : chooser.showOpenDialog(this);
    }

    // Styles each nested control inside the file chooser.
    private void styleFileChooserComponentTree(Component component, Color bg, Color panel, Color editor, Color fg, Color border, Color buttonBg, Color accent) {
        if (component instanceof JPanel) {
            component.setBackground(bg);
            ((JPanel) component).setOpaque(true);
        } else if (component instanceof JList || component instanceof JTable || component instanceof JTree) {
            component.setBackground(editor);
            component.setForeground(fg);
        } else if (component instanceof JTextField) {
            component.setBackground(editor);
            component.setForeground(fg);
            ((JTextField) component).setCaretColor(fg);
            ((JTextField) component).setBorder(new CompoundBorder(new LineBorder(border, 1, true), new EmptyBorder(7, 10, 7, 10)));
        } else if (component instanceof JComboBox) {
            styleComboBox((JComponent) component, editor, fg, border);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            button.setFocusPainted(false);
            button.setFont(uiFont(Font.BOLD, 13));
            boolean primary = button.getText() != null && button.getText().equals(chooser.getApproveButtonText());
            button.setBackground(primary ? accent : buttonBg);
            button.setForeground(primary ? Color.WHITE : fg);
            button.setBorder(new CompoundBorder(
                    new LineBorder(primary ? accent : border, 1, true),
                    new EmptyBorder(7, 12, 7, 12)
            ));
        } else if (component instanceof JLabel) {
            component.setForeground(fg);
            component.setFont(uiFont(Font.BOLD, 13));
        } else if (component instanceof JScrollPane) {
            component.setBackground(editor);
            ((JScrollPane) component).getViewport().setBackground(editor);
            ((JScrollPane) component).setBorder(new LineBorder(border, 1, true));
        } else if (component instanceof JSeparator) {
            component.setForeground(border);
            component.setBackground(border);
        } else {
            component.setBackground(bg);
            component.setForeground(fg);
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                styleFileChooserComponentTree(child, bg, panel, editor, fg, border, buttonBg, accent);
            }
        }
    }

    // Builds and shows the custom confirmation dialog.
    private int showStyledConfirmDialog(String titleText, String messageText, String headlineText, String detailText) {
        Color[] palette = getThemePalette();
        Color bg = palette[0];
        Color editor = palette[2];
        Color fg = palette[3];
        Color sub = palette[4];
        Color border = palette[5];
        Color buttonBg = palette[7];
        Color accent = palette[8];

        final int[] result = {JOptionPane.CLOSED_OPTION};
        JDialog dialog = new JDialog(this, titleText, true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(bg);
        dialog.setSize(500, 240);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(bg);
        header.setBorder(new EmptyBorder(18, 20, 10, 20));
        JLabel title = new JLabel(titleText);
        title.setFont(titleFont(Font.BOLD, 22));
        title.setForeground(fg);
        header.add(title, BorderLayout.WEST);

        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(editor);
        card.setBorder(new CompoundBorder(
                new LineBorder(border, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel icon = new JLabel("!");
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(42, 42));
        icon.setOpaque(true);
        icon.setBackground(accent);
        icon.setForeground(Color.WHITE);
        icon.setFont(uiFont(Font.BOLD, 22));
        icon.setBorder(new LineBorder(accent, 1, true));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        JLabel headline = new JLabel(headlineText);
        headline.setFont(uiFont(Font.BOLD, 16));
        headline.setForeground(fg);
        JLabel message = new JLabel("<html><div style='width:280px;'>" + messageText + "</div></html>");
        message.setFont(uiFont(Font.PLAIN, 14));
        message.setForeground(sub);
        JLabel detail = new JLabel(detailText);
        detail.setFont(uiFont(Font.PLAIN, 13));
        detail.setForeground(sub);
        textBlock.add(headline);
        textBlock.add(Box.createVerticalStrut(8));
        textBlock.add(message);
        textBlock.add(Box.createVerticalStrut(6));
        textBlock.add(detail);

        card.add(icon, BorderLayout.WEST);
        card.add(textBlock, BorderLayout.CENTER);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonBar.setOpaque(false);
        buttonBar.setBorder(new EmptyBorder(14, 20, 18, 20));

        JButton saveButton = new JButton(t("Save"));
        JButton discardButton = new JButton(t("No"));
        JButton cancelButton = new JButton(t("Cancel"));

        styleDialogButton(saveButton, accent, Color.WHITE, accent);
        styleDialogButton(discardButton, buttonBg, fg, border);
        styleDialogButton(cancelButton, buttonBg, fg, border);

        saveButton.addActionListener(e -> {
            result[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });
        discardButton.addActionListener(e -> {
            result[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });
        cancelButton.addActionListener(e -> {
            result[0] = JOptionPane.CANCEL_OPTION;
            dialog.dispose();
        });

        buttonBar.add(saveButton);
        buttonBar.add(discardButton);
        buttonBar.add(cancelButton);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(card, BorderLayout.CENTER);
        dialog.add(buttonBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
        return result[0];
    }

    // Styles a button used inside custom dialogs.
    private void styleDialogButton(JButton button, Color bg, Color fg, Color border) {
        button.setFont(uiFont(Font.BOLD, 13));
        button.setPreferredSize(new Dimension(104, 38));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(border, 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));
    }

    // Applies the light or dark theme colors to the whole interface.
    private void applyTheme() {
        boolean dark = theme.equalsIgnoreCase("Dark");

        Color bg = dark ? new Color(20, 24, 32) : new Color(243, 238, 229);
        Color panel = dark ? new Color(30, 36, 48) : new Color(234, 226, 213);
        Color editor = dark ? new Color(14, 18, 26) : new Color(255, 252, 246);
        Color fg = dark ? new Color(238, 239, 243) : new Color(48, 39, 30);
        Color sub = dark ? new Color(155, 166, 184) : new Color(121, 105, 88);
        Color border = dark ? new Color(61, 72, 92) : new Color(206, 193, 175);
        Color cardBorder = dark ? new Color(55, 67, 88) : new Color(198, 184, 163);
        Color buttonBg = dark ? new Color(38, 47, 63) : new Color(249, 245, 237);
        Color buttonHover = dark ? new Color(64, 104, 181) : new Color(166, 95, 42);
        Color accent = dark ? new Color(95, 140, 224) : new Color(173, 92, 38);
        Color accentSoft = dark ? new Color(55, 79, 122) : new Color(233, 212, 190);

        getContentPane().setBackground(bg);
        contentPanel.setBackground(bg);
        headerPanel.setBackground(bg);
        topBar.setBackground(panel);
        centerToolbarPanel.setBackground(panel);
        toolbarShellPanel.setBackground(editor);
        rightPanel.setBackground(panel);
        statusPanel.setBackground(bg);
        editorCardPanel.setBackground(editor);

        textPane.setBackground(editor);
        textPane.setForeground(fg);
        textPane.setCaretColor(fg);
        textPane.setSelectionColor(accentSoft);
        textPane.setSelectedTextColor(fg);
        textPane.setFont(new Font(fontFamily, Font.PLAIN, fontSize));
        textPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        appTitleLabel.setFont(titleFont(Font.BOLD, 30));
        appSubtitleLabel.setFont(uiFont(Font.PLAIN, 13));
        fileMenuButton.setFont(uiFont(Font.BOLD, 14));
        editMenuButton.setFont(uiFont(Font.BOLD, 14));
        viewMenuButton.setFont(uiFont(Font.BOLD, 14));
        helpMenuButton.setFont(uiFont(Font.BOLD, 14));
        newBtn.setFont(uiFont(Font.BOLD, 13));
        openBtn.setFont(uiFont(Font.BOLD, 13));
        saveBtn.setFont(uiFont(Font.BOLD, 13));
        replaceBtn.setFont(uiFont(Font.BOLD, 13));
        settingsBtn.setFont(uiFont(Font.BOLD, 13));
        boldBtn.setFont(uiFont(Font.BOLD, 16));
        italicBtn.setFont(uiFont(Font.ITALIC, 16));
        underlineBtn.setFont(uiFont(Font.PLAIN, 16));
        fontFamilyCombo.setFont(uiFont(Font.PLAIN, 14));
        fontSizeCombo.setFont(uiFont(Font.PLAIN, 14));

        lineNumbers.setBackground(panel);
        lineNumbers.setForeground(sub);
        lineNumbers.setBorder(new MatteBorder(0, 0, 0, 1, border));

        scrollPane.getViewport().setBackground(editor);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        headerPanel.setBorder(new EmptyBorder(0, 18, 12, 18));
        topBar.setBorder(new CompoundBorder(
                new LineBorder(border, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));
        toolbarShellPanel.setBorder(new CompoundBorder(
                new LineBorder(border, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        for (Component component : centerToolbarPanel.getComponents()) {
            if (component instanceof JPanel && component.getPreferredSize().width == 1) {
                component.setBackground(border);
            }
        }
        editorCardPanel.setBorder(new CompoundBorder(
                new LineBorder(cardBorder, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        statusPanel.setBorder(new EmptyBorder(12, 26, 20, 26));

        styleFlatButton(fileMenuButton, panel, fg);
        styleFlatButton(editMenuButton, panel, fg);
        styleFlatButton(viewMenuButton, panel, fg);
        styleFlatButton(helpMenuButton, panel, fg);
        fileMenuButton.setBorder(new EmptyBorder(8, 14, 8, 14));
        editMenuButton.setBorder(new EmptyBorder(8, 14, 8, 14));
        viewMenuButton.setBorder(new EmptyBorder(8, 14, 8, 14));
        helpMenuButton.setBorder(new EmptyBorder(8, 14, 8, 14));

        styleToolbarButton(newBtn);
        styleToolbarButton(openBtn);
        styleToolbarButton(saveBtn);
        styleToolbarButton(replaceBtn);
        styleToolbarButton(settingsBtn);

        for (JButton button : new JButton[]{newBtn, openBtn, saveBtn, replaceBtn}) {
            button.setBackground(buttonBg);
            button.setForeground(fg);
            button.setBorder(new EmptyBorder(7, 10, 7, 10));
            button.setContentAreaFilled(false);
        }
        settingsBtn.setBackground(accent);
        settingsBtn.setForeground(Color.WHITE);
        settingsBtn.setBorder(new CompoundBorder(new LineBorder(accent, 1, true), new EmptyBorder(7, 12, 7, 12)));

        styleComboBox(fontFamilyCombo, buttonBg, fg, border);
        styleComboBox(fontSizeCombo, buttonBg, fg, border);

        refreshFormatButtons();

        appTitleLabel.setForeground(fg);
        appSubtitleLabel.setForeground(sub);
        statusLabel.setForeground(fg);
        fileLabel.setForeground(sub);
    }

    // Updates all visible text to the selected language.
    private void applyLanguage() {
        fileMenuButton.setText(t("File"));
        editMenuButton.setText(t("Edit"));
        viewMenuButton.setText(t("View"));
        helpMenuButton.setText(t("Help"));

        newBtn.setText(t("New"));
        openBtn.setText(t("Open"));
        saveBtn.setText(t("Save"));
        undoBtn.setText(t("Undo"));
        redoBtn.setText(t("Redo"));
        findBtn.setText(t("Find"));
        replaceBtn.setText(t("Replace"));
        wrapBtn.setText(t("Wrap"));
        dateBtn.setText(t("Date"));
        settingsBtn.setText(t("Settings"));
    }

    // Returns one translated text value for the given key.
    private String t(String key) {
        Map<String, String> map = getDictionary();
        return map.getOrDefault(key, key);
    }

    // Creates the dictionary used for English, Chinese, and Arabic text.
    private Map<String, String> getDictionary() {
        Map<String, String> map = new LinkedHashMap<>();

        if ("中文".equals(language)) {
            map.put("File", "文件");
            map.put("Edit", "编辑");
            map.put("View", "查看");
            map.put("Help", "帮助");

            map.put("New", "新建");
            map.put("Open", "打开");
            map.put("Save", "保存");
            map.put("Save As", "另存为");
            map.put("Exit", "退出");

            map.put("Undo", "撤销");
            map.put("Redo", "重做");
            map.put("Cut", "剪切");
            map.put("Copy", "复制");
            map.put("Paste", "粘贴");
            map.put("Select All", "全选");
            map.put("Find", "查找");
            map.put("Replace", "替换");
            map.put("Insert Date/Time", "插入日期/时间");

            map.put("Zoom In", "放大");
            map.put("Zoom Out", "缩小");
            map.put("Reset Zoom", "重置缩放");
            map.put("Toggle Bold", "切换粗体");
            map.put("Toggle Italic", "切换斜体");
            map.put("Toggle Underline", "切换下划线");
            map.put("Toggle Word Wrap", "切换自动换行");
            map.put("Toggle Line Numbers", "切换行号");
            map.put("About", "关于");
            map.put("Settings", "设置");

            map.put("Wrap", "换行");
            map.put("Date", "日期");
            map.put("Theme", "主题");
            map.put("Language", "语言");
            map.put("Font Family", "字体");
            map.put("Font Size", "字号");
            map.put("Bold text", "粗体");
            map.put("Italic text", "斜体");
            map.put("Underline text", "下划线");
            map.put("Word wrap", "自动换行");
            map.put("Show line numbers", "显示行号");
            map.put("Apply", "应用");
            map.put("Close", "关闭");
            map.put("Find Next", "查找下一个");
            map.put("Find what:", "查找内容:");
            map.put("Replace with:", "替换为:");
            map.put("Confirm", "确认");
            map.put("You have unsaved changes. Save before continuing?", "你有未保存的更改。继续前是否保存？");
            map.put("Text not found", "未找到文本");
            map.put("File opened successfully", "文件打开成功");
            map.put("File saved successfully", "文件保存成功");
            map.put("Word wrap enabled", "已启用自动换行");
            map.put("Word wrap disabled", "已禁用自动换行");
            map.put("Line numbers shown", "已显示行号");
            map.put("Line numbers hidden", "已隐藏行号");
            map.put("Bold updated", "粗体已更新");
            map.put("Italic updated", "斜体已更新");
            map.put("Underline updated", "下划线已更新");
        } else if ("العربية".equals(language)) {
            map.put("File", "ملف");
            map.put("Edit", "تحرير");
            map.put("View", "عرض");
            map.put("Help", "مساعدة");

            map.put("New", "جديد");
            map.put("Open", "فتح");
            map.put("Save", "حفظ");
            map.put("Save As", "حفظ باسم");
            map.put("Exit", "خروج");

            map.put("Undo", "تراجع");
            map.put("Redo", "إعادة");
            map.put("Cut", "قص");
            map.put("Copy", "نسخ");
            map.put("Paste", "لصق");
            map.put("Select All", "تحديد الكل");
            map.put("Find", "بحث");
            map.put("Replace", "استبدال");
            map.put("Insert Date/Time", "إدراج التاريخ/الوقت");

            map.put("Zoom In", "تكبير");
            map.put("Zoom Out", "تصغير");
            map.put("Reset Zoom", "إعادة الحجم");
            map.put("Toggle Bold", "تبديل العريض");
            map.put("Toggle Italic", "تبديل المائل");
            map.put("Toggle Underline", "تبديل الخط السفلي");
            map.put("Toggle Word Wrap", "تبديل التفاف النص");
            map.put("Toggle Line Numbers", "تبديل أرقام السطور");
            map.put("About", "حول");
            map.put("Settings", "الإعدادات");

            map.put("Wrap", "التفاف");
            map.put("Date", "تاريخ");
            map.put("Theme", "السمة");
            map.put("Language", "اللغة");
            map.put("Font Family", "نوع الخط");
            map.put("Font Size", "حجم الخط");
            map.put("Bold text", "نص عريض");
            map.put("Italic text", "نص مائل");
            map.put("Underline text", "تسطير النص");
            map.put("Word wrap", "التفاف النص");
            map.put("Show line numbers", "إظهار أرقام السطور");
            map.put("Apply", "تطبيق");
            map.put("Close", "إغلاق");
            map.put("Find Next", "بحث التالي");
            map.put("Find what:", "ابحث عن:");
            map.put("Replace with:", "استبدال بـ:");
            map.put("Confirm", "تأكيد");
            map.put("You have unsaved changes. Save before continuing?", "لديك تغييرات غير محفوظة. هل تريد الحفظ قبل المتابعة؟");
            map.put("Text not found", "لم يتم العثور على النص");
            map.put("File opened successfully", "تم فتح الملف بنجاح");
            map.put("File saved successfully", "تم حفظ الملف بنجاح");
            map.put("Word wrap enabled", "تم تفعيل التفاف النص");
            map.put("Word wrap disabled", "تم إيقاف التفاف النص");
            map.put("Line numbers shown", "تم إظهار أرقام السطور");
            map.put("Line numbers hidden", "تم إخفاء أرقام السطور");
            map.put("Bold updated", "تم تحديث العريض");
            map.put("Italic updated", "تم تحديث المائل");
            map.put("Underline updated", "تم تحديث التسطير");
            map.put("YES", "نعم");
            map.put("NO", "لا");
            map.put("Cancel", "تراجع");
        } else {
            map.put("File", "File");
            map.put("Edit", "Edit");
            map.put("View", "View");
            map.put("Help", "Help");

            map.put("New", "New");
            map.put("Open", "Open");
            map.put("Save", "Save");
            map.put("Save As", "Save As");
            map.put("Exit", "Exit");

            map.put("Undo", "Undo");
            map.put("Redo", "Redo");
            map.put("Cut", "Cut");
            map.put("Copy", "Copy");
            map.put("Paste", "Paste");
            map.put("Select All", "Select All");
            map.put("Find", "Find");
            map.put("Replace", "Replace");
            map.put("Insert Date/Time", "Insert Date/Time");

            map.put("Zoom In", "Zoom In");
            map.put("Zoom Out", "Zoom Out");
            map.put("Reset Zoom", "Reset Zoom");
            map.put("Toggle Bold", "Toggle Bold");
            map.put("Toggle Italic", "Toggle Italic");
            map.put("Toggle Underline", "Toggle Underline");
            map.put("Toggle Word Wrap", "Toggle Word Wrap");
            map.put("Toggle Line Numbers", "Toggle Line Numbers");
            map.put("About", "About");
            map.put("Settings", "Settings");

            map.put("Wrap", "Wrap");
            map.put("Date", "Date");
            map.put("Theme", "Theme");
            map.put("Language", "Language");
            map.put("Font Family", "Font Family");
            map.put("Font Size", "Font Size");
            map.put("Bold text", "Bold text");
            map.put("Italic text", "Italic text");
            map.put("Underline text", "Underline text");
            map.put("Word wrap", "Word wrap");
            map.put("Show line numbers", "Show line numbers");
            map.put("Apply", "Apply");
            map.put("Close", "Close");
            map.put("Find Next", "Find Next");
            map.put("Find what:", "Find what:");
            map.put("Replace with:", "Replace with:");
            map.put("Confirm", "Confirm");
            map.put("You have unsaved changes. Save before continuing?", "You have unsaved changes. Save before continuing?");
            map.put("Text not found", "Text not found");
            map.put("File opened successfully", "File opened successfully");
            map.put("File saved successfully", "File saved successfully");
            map.put("Word wrap enabled", "Word wrap enabled");
            map.put("Word wrap disabled", "Word wrap disabled");
            map.put("Line numbers shown", "Line numbers shown");
            map.put("Line numbers hidden", "Line numbers hidden");
            map.put("Bold updated", "Bold updated");
            map.put("Italic updated", "Italic updated");
            map.put("Underline updated", "Underline updated");
        }

        return map;
    }

    // Rebuilds the line number sidebar based on editor content.
    private void updateLineNumbers() {
        String text = textPane.getText();
        int lines = 1;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(i).append(System.lineSeparator());
        }

        lineNumbers.setText(sb.toString());
    }

    // Updates the status bar with line, column, characters, and file name.
    private void updateStatus() {
        int caret = textPane.getCaretPosition();
        String text = textPane.getText();

        int line = 1;
        int column = 1;

        for (int i = 0; i < caret && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
        }

        statusLabel.setText("Line: " + line + "  Col: " + column + "  Chars: " + text.length());
        fileLabel.setText(currentFile == null ? "Untitled" : currentFile.getName());
    }

    // Shows a custom message in the status area.
    private void updateStatusMessage(String message) {
        statusLabel.setText(message);
    }

    // Updates the window title and unsaved marker.
    private void updateTitle() {
        String name = currentFile == null ? "Untitled" : currentFile.getName();
        setTitle((modified ? "* " : "") + name + " - Advanced Notepad");
    }

    // Shows the About dialog.
    private void showAbout() {
        JOptionPane.showMessageDialog(
                this,
                "Advanced Notepad\nRich text editor with formatting, themes, language, line numbers, find/replace, and undo/redo.",
                t("About"),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Shows an error dialog.
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Resets font and formatting back to the default typing state.
    private void resetTypingState() {
        bold = false;
        italic = false;
        underline = false;
        fontFamily = "Consolas";
        fontSize = 18;

        fontFamilyCombo.setSelectedItem(fontFamily);
        fontSizeCombo.setSelectedItem(fontSize);

        applyTypingAttributesToSet();
        textPane.setCharacterAttributes(typingAttributes, false);
        refreshFormatButtons();
    }

    private static class CheckBoxIcon implements Icon {
        private final Color borderColor;
        private final Color fillColor;
        private final Color checkColor;

        CheckBoxIcon(Color borderColor, Color fillColor, Color checkColor) {
            this.borderColor = borderColor;
            this.fillColor = fillColor;
            this.checkColor = checkColor;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(x, y, 16, 16, 5, 5);
            g2.setColor(borderColor);
            g2.drawRoundRect(x, y, 16, 16, 5, 5);
            if (checkColor.getAlpha() > 0) {
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(checkColor);
                g2.drawLine(x + 4, y + 9, x + 7, y + 12);
                g2.drawLine(x + 7, y + 12, x + 12, y + 5);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    private static class GearIcon implements Icon {
        private final int size;
        private final Color color;

        GearIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);

            int center = size / 2;
            int inner = Math.max(3, size / 5);
            int outer = Math.max(inner + 2, size / 2 - 1);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int i = 0; i < 8; i++) {
                double angle = Math.toRadians(i * 45.0);
                int x1 = center + (int) ((inner + 2) * Math.cos(angle));
                int y1 = center + (int) ((inner + 2) * Math.sin(angle));
                int x2 = center + (int) (outer * Math.cos(angle));
                int y2 = center + (int) (outer * Math.sin(angle));
                g2.drawLine(x1, y1, x2, y2);
            }

            g2.drawOval(center - inner, center - inner, inner * 2, inner * 2);
            g2.fillOval(center - 1, center - 1, 3, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
