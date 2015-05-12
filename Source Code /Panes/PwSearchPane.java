package Panes;

import Objects.EcWithPathway;
import Objects.PathwayWithEc;
import Objects.Project;
import Objects.Sample;
import Prog.PathButt;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

// The panel which comes up in the GUI by using the drop down menu option Projects->Search
// This allows the user to search by EC or by pathway

public class PwSearchPane extends JPanel {
	private static final long serialVersionUID = 1L; 
	private ArrayList<Sample> samples_; // ArrayList of samples to search through
	private Sample overSample_; 
	private ArrayList<PathwayWithEc> pathways_; // ArrayList of pathways to search through
	private JTextField searchfield; // Pathway search field
	private JTextField searchfield2; // EC search field
	private ArrayList<Integer> pathwIndexes_; // Temp variable of the indicies in the pathways arraylist that the search hits
	private ArrayList<Integer> ecIndexes_; // Temp variable of the indicies in the ec arraylist that the search hits
	private Project proj_; // The active project
	private int line_; 
	private int linDis = 40; 
	private int colDis = 250; 
	private int xDist = 250; 
	private JPanel optionsPanel_; // Options panel
	private JPanel displayP_; // Panel where searches are displayed
	private JScrollPane showJPanel_; // The scroll panel fro if the display panel gets too large for its allotted space
	private JLabel mouseOverDisp; 
	private JPanel mouseOverP; 

	public PwSearchPane(Project proj, ArrayList<Sample> samples,
			Sample overallSample, Dimension dim) {
		this.samples_ = new ArrayList();
		this.proj_ = proj;
		for (int smpCnt = 0; smpCnt < samples.size(); smpCnt++) {
			this.samples_.add((Sample) samples.get(smpCnt));
		}
		this.overSample_ = overallSample;
		setVisible(true);
		setLayout(new BorderLayout());
		setBackground(Color.orange);
		setSize(dim);

		findPw();

		invalidate();
		validate();
		repaint();
	}

	private void initMainPanels() {
		this.optionsPanel_ = new JPanel();
		this.optionsPanel_.setPreferredSize(new Dimension(getWidth(), 100));
		this.optionsPanel_.setLocation(0, 0);
		this.optionsPanel_.setBackground(Project.getBackColor_().darker());
		this.optionsPanel_.setVisible(true);
		this.optionsPanel_.setLayout(null);
		add(this.optionsPanel_, "First");

		this.displayP_ = new JPanel();
		this.displayP_.setLocation(0, 0);
		this.displayP_.setPreferredSize(new Dimension(getWidth()
				+ Project.samples_.size() * 300, 12000));
		this.displayP_.setSize(getPreferredSize());
		this.displayP_.setBackground(Project.getBackColor_());
		this.displayP_.setVisible(true);
		this.displayP_.setLayout(null);

		this.showJPanel_ = new JScrollPane(this.displayP_);
		this.showJPanel_.setVisible(true);
		this.showJPanel_.setVerticalScrollBarPolicy(20);
		this.showJPanel_.setHorizontalScrollBarPolicy(30);

		add("Center", this.showJPanel_);
	}

	private void findPw() {
		removeAll();
		initMainPanels();
		this.pathwIndexes_ = new ArrayList();
		this.line_ = 0;

		this.searchfield = new JTextField("Enter Pathway-ID");
		this.searchfield.setBounds(100, 200, 200, 25);
		this.searchfield.setVisible(true);
		this.searchfield.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tmp = PwSearchPane.this.searchfield.getText();
				PwSearchPane.this.searchPw(tmp);
				if (PwSearchPane.this.pathwIndexes_.size() > 0) {
					PwSearchPane.this.showAllPaths();
				}
				PwSearchPane.this.invalidate();
				PwSearchPane.this.validate();
				PwSearchPane.this.repaint();
			}
		});
		this.displayP_.add(this.searchfield);

		JButton button_ = new JButton();
		button_.setBounds(325, 200, 100, 25);
		button_.setVisible(true);
		button_.setText("search");
		button_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tmp = PwSearchPane.this.searchfield.getText();
				PwSearchPane.this.searchPw(tmp);
				if (PwSearchPane.this.pathwIndexes_.size() > 0) {
					PwSearchPane.this.showAllPaths();
				}
				PwSearchPane.this.invalidate();
				PwSearchPane.this.validate();
				PwSearchPane.this.repaint();
			}
		});
		this.displayP_.add(button_);

		this.pathwIndexes_ = new ArrayList();
		this.line_ = 0;

		this.searchfield2 = new JTextField("Enter Ec-ID");
		this.searchfield2.setBounds(500, 200, 200, 25);
		this.searchfield2.setVisible(true);
		this.searchfield2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tmp = PwSearchPane.this.searchfield2.getText();
				PwSearchPane.this.searchEc(tmp);
				if (PwSearchPane.this.ecIndexes_.size() > 0) {
					PwSearchPane.this.showEcs();
				}
				PwSearchPane.this.invalidate();
				PwSearchPane.this.validate();
				PwSearchPane.this.repaint();
			}
		});
		this.displayP_.add(this.searchfield2);

		JButton button2_ = new JButton();
		button2_.setBounds(725, 200, 100, 25);
		button2_.setVisible(true);
		button2_.setText("search");
		button2_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tmp = PwSearchPane.this.searchfield2.getText();

				PwSearchPane.this.searchEc(tmp);
				if (PwSearchPane.this.ecIndexes_.size() > 0) {
					PwSearchPane.this.showEcs();
				}
				PwSearchPane.this.invalidate();
				PwSearchPane.this.validate();
				PwSearchPane.this.repaint();
			}
		});
		this.displayP_.add(button2_);
	}

	private void searchPw(String in) {
		String lowcase = in.replaceFirst(in.substring(0, 1), in.substring(0, 1)
				.toUpperCase());
		String upCase = in.replaceFirst(in.substring(0, 1), in.substring(0, 1)
				.toLowerCase());
		this.pathwIndexes_ = new ArrayList();
		for (int pwCnt = 0; pwCnt < this.overSample_.pathways_.size(); pwCnt++) {
			if ((((PathwayWithEc) this.overSample_.pathways_.get(pwCnt)).id_
					.contains(lowcase))
					|| (((PathwayWithEc) this.overSample_.pathways_.get(pwCnt)).id_
							.contains(upCase))) {
				this.pathwIndexes_.add(Integer.valueOf(pwCnt));
			}
		}
		for (int pwCnt = 0; pwCnt < this.overSample_.pathways_.size(); pwCnt++) {
			if ((((PathwayWithEc) this.overSample_.pathways_.get(pwCnt)).name_
					.contains(lowcase))
					|| (((PathwayWithEc) this.overSample_.pathways_.get(pwCnt)).name_
							.contains(upCase))) {
				boolean notInList = true;
				for (int i = 0; i < this.pathwIndexes_.size(); i++) {
					if (((Integer) this.pathwIndexes_.get(i)).intValue() == pwCnt) {
						notInList = false;
					}
				}
				if (notInList) {
					this.pathwIndexes_.add(Integer.valueOf(pwCnt));
				}
			}
		}
	}

	private void searchEc(String in) {
		this.ecIndexes_ = new ArrayList();
		for (int ecCnt = 0; ecCnt < this.overSample_.ecs_.size(); ecCnt++) {
			if (((EcWithPathway) this.overSample_.ecs_.get(ecCnt)).name_
					.equalsIgnoreCase(in)) {
				this.ecIndexes_.add(Integer.valueOf(ecCnt));
			}
		}
		for (int ecCnt = 0; ecCnt < this.overSample_.ecs_.size(); ecCnt++) {
			if (((EcWithPathway) this.overSample_.ecs_.get(ecCnt)).name_
					.startsWith(in)) {
				boolean notInList = true;
				for (int i = 0; i < this.ecIndexes_.size(); i++) {
					if (((Integer) this.ecIndexes_.get(i)).intValue() == ecCnt) {
						notInList = false;
					}
				}
				if (notInList) {
					this.ecIndexes_.add(Integer.valueOf(ecCnt));
				}
			}
		}
	}

	private void showAllPaths() {
		removeAll();
		initMainPanels();
		showaddInfoPanel();

		this.line_ = 0;
		JButton newSearch = new JButton("New Search");
		newSearch.setBounds(40, 20, this.colDis, this.linDis);
		newSearch.setVisible(true);
		newSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PwSearchPane.this.removeAll();
				PwSearchPane.this.findPw();
				PwSearchPane.this.invalidate();
				PwSearchPane.this.validate();
				PwSearchPane.this.repaint();
			}
		});
		this.optionsPanel_.add(newSearch);
		for (int indCnt = 0; indCnt < this.pathwIndexes_.size(); indCnt++) {
			showPathway(((Integer) this.pathwIndexes_.get(indCnt)).intValue());
		}
	}

	private void showaddInfoPanel() {
		this.mouseOverP = new JPanel();
		this.mouseOverP.setBackground(Project.getBackColor_());
		this.mouseOverP.setBounds(700, 10, 500, 60);
		this.optionsPanel_.add(this.mouseOverP);

		this.mouseOverDisp = new JLabel("Additional Pathway-information");

		this.mouseOverDisp.setBounds(0, 0, 500, 60);
		this.mouseOverP.add(this.mouseOverDisp);
	}

	private void showPathway(int index) {
		this.pathways_ = this.overSample_.pathways_;

		this.displayP_.setSize(getWidth(), 100 + this.pathways_.size()
				* this.linDis);

		final PathwayWithEc opath = (PathwayWithEc) this.pathways_.get(index);

		PathButt pathNames = new PathButt(this.samples_, this.overSample_,
				(PathwayWithEc) this.pathways_.get(index), getBackground(), "",
				0);
		pathNames.setBounds(this.xDist - this.colDis, this.linDis + this.linDis
				* this.line_, this.colDis, this.linDis);
		pathNames.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
				PwSearchPane.this.mouseOverDisp.setVisible(false);

				PwSearchPane.this.mouseOverDisp
						.setText("Additional Pathway-information");
			}

			public void mouseEntered(MouseEvent e) {
				PwSearchPane.this.mouseOverDisp.setVisible(true);

				PwSearchPane.this.setAdditionalInfo(opath);
			}

			public void mouseClicked(MouseEvent e) {
			}
		});
		this.displayP_.add(pathNames);

		int sampleNr = this.samples_.size();

		this.displayP_.setSize(getWidth(), 100 + sampleNr * this.linDis);
		for (int sampleCnt = 0; sampleCnt < sampleNr; sampleCnt++) {
			Sample tmpSample = (Sample) this.samples_.get(sampleCnt);

			JLabel name = new JLabel(tmpSample.name_);
			int x = this.colDis * (sampleCnt + 1);
			int y = this.linDis;
			name.setBounds(x + 5, 20, this.colDis - 10, 20);
			this.displayP_.add(name);
			final PathwayWithEc fpath = new PathwayWithEc(
					tmpSample.getPath(((PathwayWithEc) this.pathways_
							.get(index)).id_), false);

			PathButt scores = new PathButt(this.samples_, tmpSample, fpath,
					getBackground(), "", 0);
			scores.addMouseListener(new MouseListener() {
				public void mouseReleased(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
					PwSearchPane.this.mouseOverDisp.setVisible(false);

					PwSearchPane.this.mouseOverDisp
							.setText("Additional Pathway-information");
				}

				public void mouseEntered(MouseEvent e) {
					PwSearchPane.this.mouseOverDisp.setVisible(true);

					PwSearchPane.this.setAdditionalInfo(fpath);
				}

				public void mouseClicked(MouseEvent e) {
				}
			});
			x = this.colDis * (sampleCnt + 1);
			y = this.linDis + this.linDis * this.line_;
			scores.setBounds(x, y, this.colDis, this.linDis);
			this.displayP_.add(scores);
		}
		this.line_ += 1;
	}

	private void showEcs() {
		removeAll();
		initMainPanels();

		this.line_ = 0;

		JButton newSearch = new JButton("new search");
		newSearch.setBounds(40, 20, this.colDis, this.linDis);
		newSearch.setVisible(true);
		newSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PwSearchPane.this.removeAll();
				PwSearchPane.this.findPw();
				PwSearchPane.this.invalidate();
				PwSearchPane.this.validate();
				PwSearchPane.this.repaint();
			}
		});
		this.optionsPanel_.add(newSearch);
		this.displayP_.setSize(getWidth(), 100 + this.ecIndexes_.size()
				* this.linDis);
		for (int ecCnt = 0; ecCnt < this.ecIndexes_.size(); ecCnt++) {
			final int index = ((Integer) this.ecIndexes_.get(ecCnt)).intValue();
			JButton ecButt = new JButton(
					((EcWithPathway) this.overSample_.ecs_.get(index)).name_
							+ " "
							+ ((EcWithPathway) this.overSample_.ecs_.get(index)).amount_);
			ecButt.setBounds(20, 20 + this.linDis + this.linDis * this.line_,
					this.colDis, this.linDis);
			ecButt.setVisible(true);
			ecButt.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (PwSearchPane.this.proj_ == null) {
						System.out.println("actProjsearch");
					}
					PwInfoFrame frame = new PwInfoFrame(
							(EcWithPathway) PwSearchPane.this.overSample_.ecs_
									.get(index), PwSearchPane.this.proj_,
							PwSearchPane.this.overSample_);
				}
			});
			this.displayP_.add(ecButt);
			this.line_ += 1;
		}
	}

	private void setAdditionalInfo(PathwayWithEc path) {
		this.mouseOverDisp.setText("<html>" + path.name_ + "<br>ID:" + path.id_
				+ "<br> Wgt:" + path.weight_ + "| Scr:" + path.score_
				+ "</html>");
	}
}
