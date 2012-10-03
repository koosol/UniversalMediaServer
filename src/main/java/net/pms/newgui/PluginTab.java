package net.pms.newgui;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Locale;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.DownloadPlugins;
import net.pms.configuration.PmsConfiguration;
import net.pms.external.ExternalFactory;
import net.pms.external.ExternalListener;
import net.pms.util.FormLayoutUtil;

public class PluginTab {
	private final PmsConfiguration configuration;
	
	private static final String COL_SPEC = "left:pref, 2dlu, p, 2dlu , p, 2dlu, p, 2dlu, pref:grow";
	private static final String ROW_SPEC = "p, 0dlu, p, 0dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p";
	private JPanel pPlugins;
	
	PluginTab(PmsConfiguration configuration) {
		this.configuration = configuration;
		pPlugins = null;
	}
	
	public JComponent build() {
		
		Locale locale = new Locale(configuration.getLanguage());
		ComponentOrientation orientation = ComponentOrientation.getOrientation(locale);
		String colSpec = FormLayoutUtil.getColSpec(COL_SPEC, orientation);

		FormLayout layout = new FormLayout(colSpec, ROW_SPEC);
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.DLU4_BORDER);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		
		JComponent cmp = builder.addSeparator(Messages.getString("PluginTab.1"),
				FormLayoutUtil.flip(cc.xyw(1, 1, 9), colSpec, orientation));
			cmp = (JComponent) cmp.getComponent(0);
			cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
		
		final ArrayList<DownloadPlugins> plugins = DownloadPlugins.downloadList();
		String[] cols = {Messages.getString("NetworkTab.41"), Messages.getString("NetworkTab.42"),
			Messages.getString("NetworkTab.43")};
		final JTable tab = new JTable(plugins.size() + 1, cols.length) {
			public String getToolTipText(MouseEvent e) {
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				if (rowIndex == 0) {
					return "";
				}
				DownloadPlugins plugin = plugins.get(rowIndex - 1);
				return plugin.htmlString();
			}
		};
		for (int i = 0; i < cols.length; i++) {
			tab.setValueAt(cols[i], 0, i);
		}
		tab.setCellEditor(null);
		for (int i = 0; i < plugins.size(); i++) {
			DownloadPlugins p = plugins.get(i);
			tab.setValueAt(p.getName(), i + 1, 0);
			tab.setValueAt(p.getRating(), i + 1, 1);
			tab.setValueAt(p.getAuthor(), i + 1, 2);
		}
		
		builder.add(tab, FormLayoutUtil.flip(cc.xyw(1, 7, 9), colSpec, orientation));
		
		JButton install = new JButton(Messages.getString("NetworkTab.39"));
		builder.add(install, FormLayoutUtil.flip(cc.xy(1, 14), colSpec, orientation));
		install.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!ExternalFactory.localPluginsInstalled()) {
					JOptionPane.showMessageDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())), Messages.getString("NetworkTab.40"));
					return;
				}
				final int[] rows = tab.getSelectedRows();
				JPanel panel = new JPanel();
				GridLayout layout = new GridLayout(3, 1);
				panel.setLayout(layout);
				final JFrame frame = new JFrame(Messages.getString("NetworkTab.46"));
				frame.setSize(250, 110);
				JProgressBar progressBar = new JProgressBar();
				progressBar.setIndeterminate(true);
				panel.add(progressBar);
				final JLabel label = new JLabel("");
				final JLabel inst = new JLabel("");
				panel.add(inst);
				panel.add(label);
				frame.add(panel);
				frame.setVisible(true);
				Runnable r = new Runnable() {
					public void run() {
						for (int i = 0; i < rows.length; i++) {
							if (rows[i] == 0) {
								continue;
							}
							DownloadPlugins plugin = plugins.get(rows[i] - 1);
							inst.setText(Messages.getString("NetworkTab.50") + ": " + plugin.getName());
							try {
								plugin.install(label);
							} catch (Exception e) {
							}
						}
						frame.setVisible(false);
					}
				};
				new Thread(r).start();
			}
		});
		
		cmp = builder.addSeparator(Messages.getString("NetworkTab.34"), FormLayoutUtil.flip(cc.xyw(1, 43, 9), colSpec, orientation));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		pPlugins = new JPanel(new GridLayout());
		builder.add(pPlugins, FormLayoutUtil.flip(cc.xyw(1, 45, 9), colSpec, orientation));
		
		JPanel panel = builder.getPanel();
		JScrollPane scrollPane = new JScrollPane(
				panel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		return scrollPane;
	}
	
	public void addPlugins() {
		FormLayout layout = new FormLayout(
			"fill:10:grow",
			"p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p");
		pPlugins.setLayout(layout);
		for (final ExternalListener listener : ExternalFactory.getExternalListeners()) {
			if (!appendPlugin(listener)) {
				break;
			}
		}
	}

	public boolean appendPlugin(final ExternalListener listener) {
		final JComponent comp = listener.config();
		if (comp == null) {
			return true;
		}
		CellConstraints cc = new CellConstraints();
		JButton bPlugin = new JButton(listener.name());
		// listener to show option screen
		bPlugin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showOptionDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
					comp, "Options", JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
			}
		});
		int y = pPlugins.getComponentCount() + 1;
		if (y > 30) {
			return false;
		}
		pPlugins.add(bPlugin, cc.xy(1, y));
		return true;
	}

	public void removePlugin(ExternalListener listener) {
		JButton del = null;
		for (Component c : pPlugins.getComponents()) {
			if (c instanceof JButton) {
				JButton button = (JButton) c;
				if (button.getText().equals(listener.name())) {
					del = button;
					break;
				}
			}
		}
		if (del != null) {
			pPlugins.remove(del);
			pPlugins.repaint();
		}
	}
}