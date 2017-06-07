package com.github.easai.audio.soundedit;

import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class SoundEditMenu {
	Locale locale = Locale.US;
	JMenuItem mi;
	JMenuBar mb = new JMenuBar();
	JMenu m[];
	Hashtable<JMenuItem, MENUITEM> comp = new Hashtable<>();
	static String menus[] = { "File", "Edit", "View", "Tools", "Help" };
	String menuitems[][] = { { "Open", "Save", "SaveAs", "SaveAll", "Quit" },
			{ "Divide", "Delete", "Play", "PlayAll", "Stop" }, { "Resize", "Refresh", "Split" }, { "Right", "Left" },
			{ "About" } };

	enum MENUITEM {
		nFileOpen, nFileSave, nFileSaveAs, nFileSaveAll, nFileQuit, nEditDivide, nEditDelete, nEditPlay, nEditPlayAll, nEditStop, nViewResize, nViewRefresh, nViewSplit, nToolsRight, nToolsLeft, nHelpAbout
	};

	MENUITEM mi_num[][] = {
			{ MENUITEM.nFileOpen, MENUITEM.nFileSave, MENUITEM.nFileSaveAs, MENUITEM.nFileSaveAll, MENUITEM.nFileQuit },
			{ MENUITEM.nEditDivide, MENUITEM.nEditDelete, MENUITEM.nEditPlay, MENUITEM.nEditPlayAll,
					MENUITEM.nEditStop },
			{ MENUITEM.nViewResize, MENUITEM.nViewRefresh, MENUITEM.nViewSplit },
			{ MENUITEM.nToolsRight, MENUITEM.nToolsLeft }, { MENUITEM.nHelpAbout } };

	public void setMenu(JFrame frame, ActionListener l, Locale locale) {
		this.locale = locale;
		setMenu(l);
		frame.setJMenuBar(mb);
	}

	public void setMenu(JApplet ap, ActionListener l, Locale locale) {
		this.locale = locale;
		setMenu(l);
		ap.setJMenuBar(mb);
	}

	public void setMenu(ActionListener l) {
		// setMnemonic(new MenuShortcut(KeyEvent.VK_A))
		m = new JMenu[menus.length];
		ResourceBundle menuStrings = null;
		ResourceBundle menuItemStrings = null;
		String menuTitle, shortcuts = "";
		if (locale != Locale.US) {
			menuStrings = ResourceBundle.getBundle("JavaMenuMenuMenu", locale);
			menuItemStrings = ResourceBundle.getBundle("JavaMenuMenuMenuItem", locale);
		}

		for (int i = 0; i < menus.length; i++) {
			if (locale == Locale.US) {
				menuTitle = menus[i];
			} else {
				menuTitle = menuStrings.getString(menus[i]);
			}
			m[i] = new JMenu(menuTitle);
			if (menuTitle != null && 0 < menuTitle.length()) {
				m[i].setMnemonic(menuTitle.charAt(0));
			}
			if (i != menus.length - 1) {
				mb.add(m[i]);
			}
			for (int j = 0; j < menuitems[i].length; j++) {
				String str = menuitems[i][j];
				if (locale != Locale.US) {
					str = menuItemStrings.getString(menuitems[i][j]);
				}

				if (str != null && 0 < str.length()) {

					int index = 0;
					char ch = str.charAt(index);
					int len = str.length();
					while (shortcuts.indexOf(ch) != -1 && ++index < len) {
						ch = str.charAt(index);
					}
					if (index < len) {
						mi = new JMenuItem(str, ch);
						shortcuts += ch;
					} else {
						mi = new JMenuItem(str, str.charAt(0));
					}

				} else {
					mi = new JMenuItem(str);
				}

				m[i].add(mi);

				comp.put(mi, mi_num[i][j]);
				mi.addActionListener(l);
				// if ( // disabled menuitems
				// mi_num[i][j] == nRun ||
				// ((ap != null)
				// && (mi_num[i][j] == nOpenURL
				// || mi_num[i][j] == nSave)))

				// mi.setEnabled (false);
				// else if (mi_num[i][j] == nQuit)
				// mi.setShortcut(new
				// MenuShortcut(KeyEvent.VK_Q|KeyEvent.CTRL_MASK));
			}
		}
		mb.add(Box.createHorizontalGlue());
		mb.add(m[menus.length - 1]);

	}
}
