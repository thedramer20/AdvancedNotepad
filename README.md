Advanced Notepad

A lightweight text editor built with Java Swing, designed to be simple, practical, and actually usable.

This project started as a way to understand how desktop applications work in Java. Over time, it evolved into a more complete editor with formatting, themes, and language support.

Overview

Advanced Notepad focuses on the essentials:

clean interface
responsive editing
useful features without unnecessary complexity

It’s not meant to compete with large editors, but to demonstrate how a well-structured Java application can feel polished and real.

Features
Editing
Rich text formatting (bold, italic, underline) applied only to selected text
Undo and redo with proper history handling
Find and replace
Word wrap toggle
Line numbers
Appearance
Light and dark themes
Consistent layout with a simple toolbar and status bar
Real-time cursor position and document info
Language Support
English
Chinese
Arabic

The interface updates dynamically when the language is changed.

Fonts
Multiple font families
Adjustable font size
Zoom support (keyboard and mouse)
Running the Project
Compile and run
javac AdvancedNotepad.java
java AdvancedNotepad
Using an IDE

Open the project in any Java IDE and run the main class:

IntelliJ IDEA
Eclipse
VS Code
Implementation Notes

The editor is built using standard Java Swing components:

JTextPane for rich text editing
StyledDocument for formatting control
UndoManager for undo/redo functionality
Custom UI updates for theme switching
A simple dictionary-based system for language support

The goal was to keep the architecture straightforward while still supporting real features.

Project Structure
AdvancedNotepad/
│── AdvancedNotepad.java
│── README.md
│── LICENSE
│── screenshots/
Future Improvements

Some features that could be added:

Syntax highlighting
Multi-tab editing
Auto-save
Persistent settings (using Java Preferences API)
Export options (PDF or HTML)
Contributing

Contributions are welcome.
If you want to improve the project, feel free to fork it and open a pull request.

License

MIT License

Author

thedramer20

Final Note

This project is a balance between learning and building something practical.
It shows how a desktop application can go beyond a basic example and become a usable tool.
