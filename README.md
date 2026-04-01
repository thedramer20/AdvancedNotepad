📝 Advanced Notepad

A simple but powerful text editor built using Java Swing.
I originally started this project to learn more about GUI design in Java — and it slowly turned into something much more complete.

It’s not just a basic notepad. It supports formatting, themes, multiple languages, and a bunch of quality-of-life features you’d expect from a real editor.

💡 Why this project?

Most beginner Java projects are either too simple or too abstract.
I wanted something practical, something you can actually use — so I built this.

This project helped me understand:

GUI design with Swing
Event handling
Text manipulation (StyledDocument)
Structuring a real application
✨ Features
📝 Editing
Bold, Italic, Underline (works on selected text only ✔️)
Undo / Redo
Find & Replace
Word wrap toggle
Line numbers
🎨 Appearance
Light & Dark mode
Clean, modern UI (no default ugly Swing look 😅)
Status bar with cursor position & info
🌍 Languages
English
中文 (Chinese)
العربية (Arabic)

You can switch language directly from settings.

🔤 Fonts
Multiple font families (Consolas, Arial, etc.)
Adjustable font size
Zoom in/out with shortcuts or mouse
⌨️ Shortcuts (the important ones)
Action	Shortcut
New	Ctrl + N
Open	Ctrl + O
Save	Ctrl + S
Undo	Ctrl + Z
Redo	Ctrl + Y
Bold	Ctrl + B
Italic	Ctrl + I
Underline	Ctrl + U
Find	Ctrl + F
🚀 How to run
1. Compile and run manually
javac AdvancedNotepad.java
java AdvancedNotepad
2. Using an IDE

Just open the file and run main()
Works fine in:

IntelliJ IDEA
Eclipse
VS Code
🧠 How it works (short explanation)
Uses JTextPane for rich text editing
Styling is handled with StyledDocument
Undo/Redo is managed with UndoManager
Themes are applied by dynamically updating component colors
Language system is based on a simple dictionary approach
📁 Project Structure
AdvancedNotepad/
│── AdvancedNotepad.java
│── README.md
│── LICENSE
│── screenshots/
🔧 Things I might add later
Syntax highlighting (for code)
Tabs (multiple files)
Auto-save
Better settings persistence
Export to PDF
🤝 Contributing

If you want to improve something, feel free:

Fork it
Change it
Open a PR

Even small improvements are welcome 👍

📄 License

MIT License — use it however you want.

👨‍💻 Author

Made by thedramer20

💬 Final note

This project is not meant to replace professional editors —
it’s more of a learning + practical project that turned into something usable.
