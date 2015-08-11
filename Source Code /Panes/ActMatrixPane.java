package Panes;

import Objects.ConvertStat;
import Objects.EcNr;
import Objects.EcWithPathway;
import Objects.Line;
import Objects.Pathway;
import Objects.PathwayWithEc;
import Objects.Project;
import Objects.Sample;
import Prog.Controller;
import Prog.DataProcessor;
import Prog.MetaProteomicAnalysis;
import Prog.NewFrompFrame;
import Prog.tableAndChartData;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.*;

import java.io.PrintWriter;
import java.io.File;
import java.util.LinkedHashMap;

import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.io.FastaReader;
import org.biojava.nbio.core.sequence.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;

//This is the activity Matrix Pane. Ec oriented part of the EC activity section. From here you can generate the Ec activity matrix
public class ActMatrixPane extends JPanel {
	private static final long serialVersionUID = 1L; //
	private ArrayList<Sample> smpList_; // ArrayList of samples to be used to generate the matrix
	private JButton export_; // Button to export the matrix to a file
	private JButton names_; // Tmp variable for building a a list of EC buttons
	private JLabel label_; 
	private JCheckBox bySumcheck_; // Check box used to sort matrix by sum
	private JCheckBox useOddsrat_; // Checkbox to include the odds-ratio in the
									// calculation of the matrix
	private JCheckBox moveUnmappedToEnd; // Checkbox to moved unmapped ecs to the end of the list. Default is checked
	private JCheckBox includeRepseq_; // Checkbox to include the ability to click on ecs from samples and see the sequence ids which are associated with that ec
	private JCheckBox showOptions_; // Checkbox to show the options panel
	private JCheckBox dispIncomplete_; // Checkbox to display incomplete ecs
	private JCheckBox useCsf_; // Checkbox to use CSF
	private JTextField maxVisField_; 
	private float maxVisVal_ = 0.0F; 
	private ArrayList<JLabel> nameLabels_; // ArrayList of labels
	private Project actProj_; // The active project
	private boolean sortedEc = false; 
	private boolean drawChart = false; 
	private final int xSize = 130; 
	private final int ySize = 15; 
	private int sumIndexSmp; 
	private int numChart = 0;
	private JButton resort_; // JBotton to resort the array
	private ArrayList<Line> ecMatrix_; // Arraylist of line objects used to build the matrix
	private Loadingframe lframe; // Loading frame
	private Line unmappedSum; 
	private Line mappedSum; 
	private Line incompleteSum; 
	private Line sums; 
	private tableAndChartData returnData;
	JPanel optionsPanel_; // Options panel
	JPanel displayP_; // Panel which displays the ec matrix
	JScrollPane showJPanel_; // Scroll pane which allows the user to scroll through the matrix if it is bigger than the allotted space
	DataProcessor proc_; // Data Processor which allows the input files to be parsed and for relivent data to be computed from them
	int selectedSampIndex_ = -1; 
	JLabel selectedSampText; 
	final String basePath_ = new File(".").getAbsolutePath() + File.separator; 
	JPopupMenu menuPopup;// Popup menu used for right clicking and exporting sequence ids
	JPopupMenu ecMenuPopup;
	int popupIndexY; // Coordinates to facilitate the exporting of sequence ids
	int popupIndexX; 
	int yIndex1 = 520;
	int yIndex2 = 0;
	String buttonName;
	boolean exportAll = false;
	boolean findLca = false;
	ArrayList<String> ec_list;
	String fileName = "";
	String sampleName = "";
	String chosen_ec = "";
	String chosen_sample = "";

	
	public ActMatrixPane(Project actProj, ArrayList<EcWithPathway> ecList,
			DataProcessor proc, Dimension dim) {
		this.lframe = new Loadingframe(); // opens the loading frame
		this.lframe.bigStep("Preparing Panel"); 
		this.lframe.step("Init"); 
									
		this.actProj_ = actProj; // sets this active project
		this.proc_ = proc; // stes this data processor
							
							
		setLayout(new BorderLayout()); 
		setVisible(true); 
		setBackground(Project.getBackColor_()); 
		setSize(dim); 
						
		this.smpList_ = Project.samples_; 
		ec_list = new ArrayList<String>();
		this.sumIndexSmp = 0; 
		setSelectedEc(); // Sets whether or not each sample is selected
		prepMatrix(); // Builds the ec matrixLoadingframe.close();
		initMainPanels(); // Instanciates the options, display and scroll panels
		prepaint(); // Removes everything from the back panel adds the options panel, draws the sample names, shows the ec matrix, then repaints the back panel
		Loadingframe.close(); // closes the loading frame
	}
	
	public ActMatrixPane(){
		
	}
	
	public ActMatrixPane(Project actProj,DataProcessor proc, Dimension dim) {
		this.lframe = new Loadingframe(); // opens the loading frame
		this.lframe.bigStep("Preparing Panel"); 
		this.lframe.step("Init"); 
									
		this.actProj_ = actProj; // sets this active project
		this.proc_ = proc; // stes this data processor
		ec_list = new ArrayList<String>();				
							
		setLayout(new BorderLayout()); 
		setVisible(true); 
		setBackground(Project.getBackColor_()); 
		setSize(dim); 
						
		this.smpList_ = Project.samples_; 
		this.sumIndexSmp = 0; 
		setSelectedEc(); // Sets whether or not each sample is selected
		prepMatrix(); // Builds the ec matrix
		initMainPanels(); // Instanciates the options, display and scroll panels
		prepaintLCA(); // Removes everything from the back panel adds the options panel, draws the sample names, shows the ec matrix, then repaints the back panel
		Loadingframe.close(); // closes the loading frame
	}

	private void prepaint() { // This method rebuids the back panel of the window
		removeAll(); // Removes everything on the backpanel
		initMainPanels(); // instanciates the options, display, and scroll panels
		addOptions(); // adds the buttons, labels, checkboxes etc to the options panel
		drawSampleNames(); // Draws the mouse over lables above the ec matrix. if you mouse over these lables the expand showing their full names.
		showEcValues(); // paints the ec matrix showing the ec values, calls showValues, or show odds.
		invalidate(); 
		validate(); 
		repaint(); 
	}
	
	private void prepaintLCA() { // This method rebuids the back panel of the window
		removeAll(); // Removes everything on the backpanel
		initMainPanels(); // instanciates the options, display, and scroll panels
		prepMatrix();
		addOptionsLCA(); // adds the buttons, labels, checkboxes etc to the options panel
		if(drawChart==true){
			drawChart();
		}
		invalidate(); 
		validate(); 
		repaint(); 
	}
	
	/**
	 * Draws the tables and pie chart for the determine Lowest Common Ancestor for a given EC number
	 * or sample file.
	 * 
	 * @author Jennifer Terpstra.
	 */
	protected void drawChart(){
		final MetaProteomicAnalysis meta = new MetaProteomicAnalysis();
		PiePlot pie1=null;
		final JFreeChart chart;
		System.out.println(returnData.getFileName());
		if(returnData.getFileName()!=null){
			chart = ChartFactory.createPieChart(returnData.getFileName()
					+ " Total Taxonomy", returnData.getDataset(), true, true, false);
			pie1 = (PiePlot) chart.getPlot();
		}
		else{
			chart=null;
		}
		
		pie1.setNoDataMessage("No data available");
		pie1.setCircular(false);
		pie1.setLabelGenerator(null);
		
		JPanel panel = new ChartPanel(chart);
		JPanel panel2 = new JPanel(new BorderLayout());
		JPanel panel4 = new JPanel(new BorderLayout());
		JPanel panel3 = new JPanel(new BorderLayout());
		
		
		JButton exportPie = new JButton("Export");
		exportPie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ChartUtilities.saveChartAsPNG(new File(new File(".").getAbsolutePath() + File.separator
							+ "PieChart" + File.separator + returnData.getFileName()
							+ " Total Taxonomy" + ".png"), chart, 1000, 1000);
					infoFrame(File.separator+ "PieChart" + File.separator + returnData.getFileName(), "PieChart");
				} catch (IOException e1) {
					warningFrame("PieChart folder does not exist!");
				}
			}
		});
		JButton exportTable1 = new JButton("Export");
		exportTable1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				meta.exportExcel(returnData.getTable1(),"TotalTaxa", returnData.getFileName());
				System.out.println("Exported Total Taxa");
			}
		});
		JButton exportTable2 = new JButton("Export");
		exportTable2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				meta.exportExcel(returnData.getTable2(),"Summary", returnData.getFileName());
				System.out.println("Exported Summary");
			}
		});
		
		panel3.add(exportPie, BorderLayout.SOUTH);
		panel.setVisible(true);
		panel3.setVisible(true);
		panel.setLayout(null);
		panel3.setBounds(520,250 + (1020)*numChart , 600, 600);
		panel3.add(panel);
		
		JScrollPane tableContainer = new JScrollPane(returnData.getTable1());
		panel2.add(tableContainer, BorderLayout.CENTER);
		panel2.add(exportTable1, BorderLayout.NORTH);
		panel2.setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (), returnData.getFileName()+
				"Total Taxonomy",TitledBorder.CENTER,TitledBorder.TOP));
		panel2.setVisible(true);
		panel2.setBounds(0, yIndex2 + (yIndex1 + 510)*numChart, 500,500);
		
		JScrollPane tableContainer2 = new JScrollPane(returnData.getTable2());
		panel4.add(tableContainer2, BorderLayout.CENTER);
		panel4.add(exportTable2, BorderLayout.NORTH);
		panel4.setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (), returnData.getFileName()+
				" Summary",TitledBorder.CENTER,TitledBorder.TOP));
		panel4.setVisible(true);
		panel4.setBounds(0, yIndex1 + (yIndex1 + 510)*numChart, 500,500);
		
		displayP_.add(panel3, BorderLayout.LINE_END);
		displayP_.add(panel2, BorderLayout.LINE_START);
		displayP_.add(panel4, BorderLayout.LINE_START);
		
		numChart++;
	}

	private void initMainPanels() {// instanciates the options, display, and scroll panels
		this.optionsPanel_ = new JPanel();
		this.optionsPanel_.setPreferredSize(new Dimension(getWidth(), 100));
		this.optionsPanel_.setLocation(0, 0);
		this.optionsPanel_.setBackground(Project.standard);
		this.optionsPanel_.setVisible(true);
		this.optionsPanel_.setLayout(null);
		add(this.optionsPanel_, "First");

		this.displayP_ = new JPanel();
		this.displayP_.setLocation(0, 0);
		this.displayP_.setPreferredSize(new Dimension(
				(Project.samples_.size() + 2) * 130,
				(this.ecMatrix_.size() + 2) * 15 + 100));
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

	private void prepMatrix() {// This preps the onscreen matrix using the arrayline_[] variable in the line object.
		this.ecMatrix_ = new ArrayList<Line>();
		this.lframe.bigStep("Preparing Matrix");
		this.lframe.step("Init");
		this.sumIndexSmp = 0;
		for (int x = 0; x < Project.samples_.size(); x++) {
			if ((((Sample) Project.samples_.get(x)).inUse)
					&& (((Sample) Project.samples_.get(x)).onoff)) {
				this.sumIndexSmp += 1;
			}
		}
		int indexSmp = -1;

		this.mappedSum = new Line(new double[this.sumIndexSmp], true, false,
				false);
		this.mappedSum.fillWithZeros();
		this.unmappedSum = new Line(new double[this.sumIndexSmp], false, true,
				false);
		this.unmappedSum.fillWithZeros();
		this.incompleteSum = new Line(new double[this.sumIndexSmp], false,
				false, true);
		this.incompleteSum.fillWithZeros();
		this.sums = new Line(new double[this.sumIndexSmp], false, false, false);
		this.sums.fillWithZeros();
		for (int x = 0; x < Project.samples_.size(); x++) {
			if ((((Sample) Project.samples_.get(x)).inUse)
					&& (((Sample) Project.samples_.get(x)).onoff)) {
				indexSmp++;
				ArrayList<EcWithPathway> actSample = ((Sample) Project.samples_
						.get(x)).ecs_;
				for (int ecCnt = 0; ecCnt < actSample.size(); ecCnt++) {
					EcWithPathway actEc = (EcWithPathway) actSample.get(ecCnt);
					actEc.addStats();
					if (actEc.isSelected_) {
						boolean found = false;
						this.lframe.step(actEc.name_);
						for (int arrCnt = 0; arrCnt < this.ecMatrix_.size(); arrCnt++) {
							if (actEc.name_
									.contentEquals(((Line) this.ecMatrix_
											.get(arrCnt)).getEc_().name_)) {
								found = true;
								((Line) this.ecMatrix_.get(arrCnt)).arrayLine_[indexSmp] = actEc.amount_;
								this.sums.arrayLine_[indexSmp] += actEc.amount_;
								if ((actEc.isCompleteEc()) || (actEc.userEC)) {
									if (actEc.isUnmapped()) {
										this.unmappedSum.arrayLine_[indexSmp] += actEc.amount_;
										break;
									}
									this.mappedSum.arrayLine_[indexSmp] += actEc.amount_;
									break;
								}
								this.incompleteSum.arrayLine_[indexSmp] += actEc.amount_;

								break;
							}
						}
						if (!found) {
							Line line = new Line(new EcWithPathway(actEc),
									new double[this.sumIndexSmp]);
							line.fillWithZeros();
							line.arrayLine_[indexSmp] = actEc.amount_;
							this.sums.arrayLine_[indexSmp] += actEc.amount_;
							if ((actEc.isCompleteEc()) || (actEc.userEC)) {
								if (actEc.isUnmapped()) {
									this.unmappedSum.arrayLine_[indexSmp] += actEc.amount_;
								} else {
									this.mappedSum.arrayLine_[indexSmp] += actEc.amount_;
								}
							} else {
								this.incompleteSum.arrayLine_[indexSmp] += actEc.amount_;
							}
							this.ecMatrix_.add(line);
						}
					}
				}
			}
		}
		for (int arrCnt = 0; arrCnt < this.ecMatrix_.size(); arrCnt++) {
			((Line) this.ecMatrix_.get(arrCnt)).setSum();
		}
		this.mappedSum.setSum();
		this.unmappedSum.setSum();
		this.incompleteSum.setSum();
		this.sums.setSum();
		unCompleteMover();
	}
	
	//Options panel for the Lowest Common Ancestor page
	private void addOptionsLCA(){
		
		int ecCnt;
		EcWithPathway ec_Path = null;
		ec_list = new ArrayList<String>();
		//adding all the ec numbers found within the ecMatrix to the LCA EC number combobox
		for (ecCnt = 0; ecCnt < this.ecMatrix_.size(); ecCnt++) {
			Line ecNr = (Line) this.ecMatrix_.get(ecCnt);
			if (ecNr.getEc_().isCompleteEc()) {
				if (ecNr.getEc_() != null) {
					ec_Path = ecNr.getEc_();
				}
				if (ec_Path != null && ec_Path.getName_() != null) {
					ec_list.add(ec_Path.getName_());
				}
			}
		}
		Collections.sort(ec_list);
		
		//Combo box of the various ec numbers to find their lowest common ancestor
		final JComboBox<String> ecList = new JComboBox<String>();
			System.out.println("ec list size: " + ec_list.size());
			ecList.addItem("No EC Selected");
			for (int i = 0; i < ec_list.size(); i++) {
				if (ec_list.size() > 0) {
					ecList.addItem(ec_list.get(i));
				}
			}
		ecList.addActionListener(new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
			 System.out.println(ecList.getSelectedItem().toString());
			 chosen_ec = ecList.getSelectedItem().toString();
		    }
		  });
		ecList.setSelectedItem(0);
		ecList.setBounds(20,35,150,40);
		ecList.setVisible(true);
		
		//combo box of all the samples present within the project, can choose to find lca of specific ec and sample
		final JComboBox<String> sampleList = new JComboBox<String>();
			//can find lca of all samples given just the ec number
			sampleList.addItem("All Samples");
			for (int j = 0; j < Project.samples_.size(); j++) {
				sampleList.addItem(Project.samples_.get(j).name_);
			}
			sampleList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					 System.out.println(sampleList.getSelectedItem().toString());
					chosen_sample = sampleList.getSelectedItem().toString();
				}
			});
		sampleList.setSelectedItem(0);
		sampleList.setBounds(210, 35, 350, 40);
		sampleList.setVisible(true);
		
		this.export_ = new JButton("Find Lowest Common Ancestor");
		this.export_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean oneFile = false;
				if (!chosen_ec.equals("No EC Selected")&&!chosen_ec.equals("")) {
				lframe = new Loadingframe(); // opens the loading frame
				lframe.bigStep("Calculating LCA..");
				lframe.step(chosen_ec);

				LinkedHashMap<String,String> seq_for_lca;
				//If the user did not choose a sample, export all the samples into one file
				if(chosen_sample.equals("")||chosen_sample.equals("All Samples")){
					oneFile = true;
					exportAll = true;
				}
				else{
					oneFile = false;
					exportAll = false;
				}
				seq_for_lca = cmdExportSequences(chosen_ec,sampleName, oneFile, findLca);
				oneFile = false;
				exportAll = false;
				
				if(((chosen_sample.equals("All Samples")||(chosen_sample.equals("")))&&!chosen_ec.equals("No EC Selected"))){
					fileName = chosen_ec+"-";
					
				}
				else if(((!chosen_sample.equals("All Samples"))||(!chosen_sample.equals(""))&&!chosen_ec.equals("No EC Selected"))){
					fileName = chosen_sample + "-" + chosen_ec + "-";
					lframe.step(chosen_sample + "-" + chosen_ec + "-");
				}
				if(!chosen_ec.equals("No EC Selected")&&!chosen_ec.equals("")){
				//cannot find the lowest common ancestor of a unselected ec number and sample
				System.out.println("Lowest Common Section");
				MetaProteomicAnalysis meta = new MetaProteomicAnalysis();
				returnData = meta.getTrypticPeptideAnaysis(meta.readFasta(seq_for_lca, fileName), oneFile);
				}

				drawChart = true;
				if (numChart < 1) {
					prepaintLCA();
				} else {
					drawChart();
				}

				Loadingframe.close();
				}
				else{
					warningFrame("Please Select an EC");
				}
				
			}
				
		});
	this.export_.setBounds(600, 35, 300, 40);
	this.export_.setVisible(true);
	this.export_.setLayout(null);
	this.export_.setForeground(Project.getFontColor_());
	
	//Button used to clear the screen of all graphs & charts
	JButton clear = new JButton("Clear Screen");
	clear.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e){
			displayP_.removeAll();
			displayP_.updateUI(); 
			numChart = 0;
			yIndex1 = 520;
			yIndex2 = 0;
		}
	});
	clear.setBounds(1000, 35, 150, 20);
	clear.setVisible(true);
	clear.setLayout(null);
	clear.setForeground(Project.getFontColor_());
	

	
	JLabel warning = new JLabel("Note: Sequence files that contain a large amount of sequences may take awhile to process");
	warning.setBounds(20, 73, 700, 20);
	warning.setVisible(true);
	warning.setLayout(null);
	warning.setForeground(Project.getFontColor_());
	
	JLabel ec_combo = new JLabel("Choose EC:");
	ec_combo.setBounds(20,15,100,20);
	ec_combo.setVisible(true);
	
	JLabel smp_combo = new JLabel("Choose Sample:");
	smp_combo.setBounds(210,15,350,20);
	smp_combo.setVisible(true);


	this.optionsPanel_.add(this.export_);
	this.optionsPanel_.add(clear);
	this.optionsPanel_.add(warning);
	this.optionsPanel_.add(ecList);
	this.optionsPanel_.add(sampleList);
	this.optionsPanel_.add(ec_combo);
	this.optionsPanel_.add(smp_combo);
	
	}
	
	private void addOptions() {// adds the buttons, labels, checkboxes etc to the options panel
		this.lframe.bigStep("Adding options");
		this.lframe.step("Buttons");
		this.optionsPanel_.removeAll();
		if (this.bySumcheck_ != null) {
			if (this.bySumcheck_.isSelected()) {
				// sortEcsbySumBubble();
				System.out.println("\nquicksort\n");
				quicksortSum();
				this.ecMatrix_ = removeDuplicates();
				System.out.println("\nquicksort done\n");
				System.out.println("bySum");
			} else {
				sortEcsbyNameQuickSort();
				this.ecMatrix_ = removeDuplicates();
				System.out.println("byName");
			}
		} else {
			sortEcsbyNameQuickSort();
			this.ecMatrix_ = removeDuplicates();
			System.out.println("byName");
		}
		if (this.showOptions_ == null) {
			this.showOptions_ = new JCheckBox("Show options");
			this.showOptions_.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ActMatrixPane.this.switchOptionsMode();
				}
			});
		}
		this.showOptions_.setVisible(true);
		this.showOptions_.setSelected(true);
		this.showOptions_.setBackground(this.optionsPanel_.getBackground());
		this.showOptions_.setForeground(Project.getFontColor_());
		this.showOptions_.setLayout(null);
		this.showOptions_.setBounds(200, 10, 150, 15);

		this.optionsPanel_.add(this.showOptions_);
		if (this.bySumcheck_ == null) {
			this.bySumcheck_ = new JCheckBox("Sort by sum");
		}
		this.bySumcheck_.setVisible(true);
		this.bySumcheck_.setLayout(null);
		this.bySumcheck_.setBackground(this.optionsPanel_.getBackground());
		this.bySumcheck_.setForeground(Project.getFontColor_());
		this.bySumcheck_.setBounds(350, 10, 150, 15);
		this.optionsPanel_.add(this.bySumcheck_);
		if (this.useOddsrat_ == null) {
			this.useOddsrat_ = new JCheckBox("Odds-ratio");
		}
		this.useOddsrat_.setVisible(true);
		this.useOddsrat_.setLayout(null);
		this.useOddsrat_.setBackground(this.optionsPanel_.getBackground());
		this.useOddsrat_.setForeground(Project.getFontColor_());
		this.useOddsrat_.setBounds(350, 27, 150, 15);
		this.optionsPanel_.add(this.useOddsrat_);
		if (this.useCsf_ == null) {
			this.useCsf_ = new JCheckBox("CSF");
		}
		this.useCsf_.setVisible(true);
		this.useCsf_.setLayout(null);
		this.useCsf_.setBackground(this.optionsPanel_.getBackground());
		this.useCsf_.setForeground(Project.getFontColor_());
		this.useCsf_.setBounds(10, 44, 100, 15);
		this.optionsPanel_.add(this.useCsf_);
		/*
		 * More powerful than the "Apply Options" button, doesn't just resort
		 * the matrix but instead rebuilds the entire matrix along with the back panel.
		 * Shouldn't be used unless the user is looking to remove samples from their
		 * view as it will take more time than the apply options button. This will be especally
		 * noticable with large samples, but much less noticable with small samples.
		 */
		this.resort_ = new JButton("Rebuild"); 

		this.resort_.setBounds(700, 50, 200, 30);
		this.resort_.setVisible(true);
		this.resort_.setLayout(null);
		this.resort_.setForeground(Project.getFontColor_());
		this.resort_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int test = 0;// A temp variable to ensure that at least on of the samples.onoff=true
				for (int i = 0; i < Project.samples_.size(); i++) {
					if (Project.samples_.get(i).onoff == true) {
						test++;
					}
				}
				/*
				 * You can't view less than one sample at a time or the program will crash,
				 * this stops the user from being able to do that and makes a window that
				 * warns the user that they will be unable to do so.
				 */
				if (test < 1) {
					final JFrame frame = new JFrame("Warning!");
					frame.setBounds(200, 200, 300, 110);
					frame.setLayout(null);
					frame.setVisible(true);

					JPanel backP = new JPanel();
					backP.setBounds(0, 0, 300, 110);
					backP.setLayout(null);
					frame.add(backP);

					JLabel label = new JLabel("Warning! You must view at least");
					label.setBounds(10, 10, 280, 15);
					backP.add(label);
					JLabel label3 = new JLabel("one sample at a time.");
					label3.setBounds(10, 25, 280, 15);
					backP.add(label3);

					JLabel label2 = new JLabel("Samples have been reset.");
					label2.setBounds(10, 50, 280, 25);
					backP.add(label2);

					for (int i = 0; i < Project.samples_.size(); i++) {
						Project.samples_.get(i).onoff = true;
					}
				}

				setSelectedEc();
				prepMatrix();
				initMainPanels();
				prepaint();
				/*
				 * After this button is pressed all sample.onoff are reset to true so that
				 * when the matrix is rebuilt again all samples will be there again. This way the user
				 * doesn't 'lose' any of their samples
				 */
				for (int i = 0; i < Project.samples_.size(); i++) {
					Project.samples_.get(i).onoff = true;
				}
			}
		});
		this.optionsPanel_.add(this.resort_);

		if (this.export_ == null) {
			this.export_ = new JButton("Write to file");
			this.export_.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fChoose_ = new JFileChooser(Project.workpath_);
					fChoose_.setFileSelectionMode(0);
					fChoose_.setBounds(100, 100, 200, 20);
					fChoose_.setVisible(true);
					File file = new File(Project.workpath_);
					fChoose_.setSelectedFile(file);
					fChoose_.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							if ((f.isDirectory())
									|| (f.getName().toLowerCase()
											.endsWith(".txt"))) {
								return true;
							}
							return false;
						}

						public String getDescription() {
							return ".txt";
						}
					});
					if (fChoose_.showSaveDialog(null) == 0) {
						try {
							String path = fChoose_.getSelectedFile()
									.getCanonicalPath();
							if (!path.endsWith(".txt")) {
								path = path + ".txt";
								System.out.println(".txt");
							}
							ActMatrixPane.this.exportMat(path,
									ActMatrixPane.this.useCsf_.isSelected());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
		}
		this.export_.setBounds(10, 10, 150, 20);
		this.export_.setVisible(true);
		this.export_.setLayout(null);
		this.export_.setForeground(Project.getFontColor_());

		this.optionsPanel_.add(this.export_);
		if (this.moveUnmappedToEnd == null) {
			this.moveUnmappedToEnd = new JCheckBox("Unmapped at end of list");
			this.moveUnmappedToEnd.setSelected(true);
		}
		this.moveUnmappedToEnd.setVisible(true);
		this.moveUnmappedToEnd.setLayout(null);
		this.moveUnmappedToEnd
				.setBackground(this.optionsPanel_.getBackground());
		this.moveUnmappedToEnd.setForeground(Project.getFontColor_());
		this.moveUnmappedToEnd.setBounds(500, 10, 200, 15);
		this.optionsPanel_.add(this.moveUnmappedToEnd);
		if (this.includeRepseq_ == null) {
			this.includeRepseq_ = new JCheckBox("Include Sequence-Ids");
			this.includeRepseq_.setSelected(false);
		}
		this.includeRepseq_.setVisible(true);
		this.includeRepseq_.setLayout(null);
		this.includeRepseq_.setForeground(Project.getFontColor_());
		this.includeRepseq_.setBackground(this.optionsPanel_.getBackground());
		this.includeRepseq_.setBounds(500, 27, 200, 15);
		this.optionsPanel_.add(this.includeRepseq_);
		if (this.dispIncomplete_ == null) {
			this.dispIncomplete_ = new JCheckBox("Display incomplete ECs");
			this.dispIncomplete_.setSelected(false);
		}
		this.dispIncomplete_.setVisible(true);
		this.dispIncomplete_.setLayout(null);
		this.dispIncomplete_.setForeground(Project.getFontColor_());
		this.dispIncomplete_.setBackground(this.optionsPanel_.getBackground());
		this.dispIncomplete_.setBounds(500, 44, 200, 15);
		this.optionsPanel_.add(this.dispIncomplete_);
		if (this.maxVisField_ == null) {
			this.maxVisField_ = new JTextField(
					(int) Math.round(this.maxVisVal_));
		}
		this.maxVisField_.setBounds(600, 10, 100, 20);
		this.maxVisField_.setLayout(null);
		this.maxVisField_.setVisible(true);

		this.resort_ = new JButton("Apply options");

		this.resort_.setBounds(700, 10, 200, 30);
		this.resort_.setVisible(true);
		this.resort_.setLayout(null);
		this.resort_.setForeground(Project.getFontColor_());
		this.resort_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ActMatrixPane.this.resort();
			}
		});
		this.optionsPanel_.add(this.resort_);
	}

	private void resort() {
		this.lframe = new Loadingframe();
		this.lframe.bigStep("resorting");
		this.sortedEc = false;

		if (this.bySumcheck_.isSelected()) {
			quicksortSum();
			this.ecMatrix_ = removeDuplicates();
		} else {
			sortEcsbyNameQuickSort();
		}

		prepaint();
		Loadingframe.close();
	}
	/*
	 * Draws the mouse over labels above the EC matrix. If you mouse over these labels
	 * they expand showing their full names. If you click on them it sets the sample.onoff=false
	 */
	private void drawSampleNames() {
		this.lframe.bigStep("drawSampleNames");

		this.nameLabels_ = new ArrayList();
		String name = "";
		if (this.selectedSampIndex_ < 0) {
			name = "Overall";
		} else {
			name = ((Sample) Project.samples_.get(this.selectedSampIndex_)).name_;
		}

		int x = 0;
		int xREAL = 0;
		for (x = 0; x < Project.samples_.size(); x++) {
			if ((((Sample) Project.samples_.get(x)).inUse)
					&& (((Sample) Project.samples_.get(x)).onoff)) {
				this.lframe.step(((Sample) this.smpList_.get(x)).name_);
				this.label_ = new JLabel(((Sample) this.smpList_.get(x)).name_);

				this.label_
						.setForeground(((Sample) this.smpList_.get(x)).sampleCol_);
				this.label_.setBounds(50 + (xREAL + 1) * 130, 20, 130, 15);
				if (this.selectedSampIndex_ == x) {
					this.label_.setBounds(this.label_.getX(),
							this.label_.getY() - 20, 400,
							this.label_.getHeight());
				}
				this.label_.setVisible(true);
				this.label_.setLayout(null);
				final int nameIndex = xREAL;

				this.nameLabels_.add(this.label_);
				this.displayP_.add(this.label_);

				final JPanel mousOverP = new JPanel();
				mousOverP.setBounds(50 + (xREAL + 1) * 130, 0, 130, 40);

				mousOverP.setBackground(Project.getBackColor_());

				mousOverP.setVisible(true);
				mousOverP.setLayout(null);
				final int index = x;
				mousOverP.addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent e) {
						Project.samples_.get(index).onoff = false;
					}

					public void mouseEntered(MouseEvent e) {
						if (ActMatrixPane.this.selectedSampIndex_ != index) {
							((JLabel) ActMatrixPane.this.nameLabels_
									.get(nameIndex)).setBounds(
									((JLabel) ActMatrixPane.this.nameLabels_
											.get(nameIndex)).getX(),
									((JLabel) ActMatrixPane.this.nameLabels_
											.get(nameIndex)).getY() - 20, 400,
									ActMatrixPane.this.label_.getHeight());
							if (ActMatrixPane.this.selectedSampIndex_ != -1) {
								((JLabel) ActMatrixPane.this.nameLabels_
										.get(ActMatrixPane.this.selectedSampIndex_))
										.setBounds(
												((JLabel) ActMatrixPane.this.nameLabels_
														.get(ActMatrixPane.this.selectedSampIndex_))
														.getX(),
												((JLabel) ActMatrixPane.this.nameLabels_
														.get(ActMatrixPane.this.selectedSampIndex_))
														.getY() + 20, 130,
												ActMatrixPane.this.label_
														.getHeight());
							}
						}
						ActMatrixPane.this.repaint();
					}

					public void mouseExited(MouseEvent e) {
						if (ActMatrixPane.this.selectedSampIndex_ != index) {
							((JLabel) ActMatrixPane.this.nameLabels_
									.get(nameIndex)).setBounds(
									((JLabel) ActMatrixPane.this.nameLabels_
											.get(nameIndex)).getX(),
									((JLabel) ActMatrixPane.this.nameLabels_
											.get(nameIndex)).getY() + 20, 130,
									ActMatrixPane.this.label_.getHeight());
							if (ActMatrixPane.this.selectedSampIndex_ != -1) {
								((JLabel) ActMatrixPane.this.nameLabels_
										.get(ActMatrixPane.this.selectedSampIndex_))
										.setBounds(
												((JLabel) ActMatrixPane.this.nameLabels_
														.get(ActMatrixPane.this.selectedSampIndex_))
														.getX(),
												((JLabel) ActMatrixPane.this.nameLabels_
														.get(ActMatrixPane.this.selectedSampIndex_))
														.getY() - 20, 400,
												ActMatrixPane.this.label_
														.getHeight());
							}
						}
						ActMatrixPane.this.repaint();
					}
					
					public void mousePressed(MouseEvent e) {
					
					}

					public void mouseReleased(MouseEvent e) {
					}
				});
				this.displayP_.add(mousOverP);
				xREAL++;

			}
		}
		this.label_ = new JLabel(" Sum");

		this.label_.setForeground(Color.black);
		this.label_.setBounds(50 + (xREAL + 1) * 130, 20, 130, 15);
		this.label_.setVisible(true);
		this.label_.setLayout(null);

		this.displayP_.add(this.label_);
	}

	private void showEcValues() {// paints the ec matrix showing the ec values, calls showValues, or show odds
		boolean sumLineDrawn = false;
		this.lframe.bigStep("showEcValues");
		int ecCnt = 0;
		for (ecCnt = 0; ecCnt < this.ecMatrix_.size(); ecCnt++) {
			Line ecNr = (Line) this.ecMatrix_.get(ecCnt);
			// System.out.println(ecNr.getEc_().name_);
			this.lframe.step(ecNr.getEc_().name_);
			if (((!ecNr.getEc_().isCompleteEc()) && (!sumLineDrawn))
					|| (ecNr.getEc_().userEC)) {
				addEcButton(this.mappedSum, ecCnt + 1);
				showValues(this.mappedSum, ecCnt + 1);

				addEcButton(this.unmappedSum, ecCnt + 2);
				showValues(this.unmappedSum, ecCnt + 2);

				sumLineDrawn = true;
				if (!this.dispIncomplete_.isSelected()) {
					break;
				}
			}
			addEcButton(ecNr, ecCnt);
			if (this.useOddsrat_.isSelected()) {
				showOdds(ecNr, ecCnt);
			} else {
				showValues(ecNr, ecCnt);
			}
		}
		if (this.dispIncomplete_.isSelected()) {
			addEcButton(this.incompleteSum, ecCnt);
			showValues(this.incompleteSum, ecCnt);
			addEcButton(this.sums, ecCnt + 1);
			showValues(this.sums, ecCnt + 1);
		} else {
			addEcButton(this.sums, ecCnt);
			showValues(this.sums, ecCnt);
		}
	}
	//prints the ec matrix, but in place of the ecs the odds ratios are used
	private void showOdds(Line ecNr, int index) {
		if (ecNr.isSumline_()) {
			addSumLineVals(ecNr, index);
			return;
		}
		int uncompleteOffset = 0;
		if (!ecNr.getEc_().isCompleteEc()) {
			uncompleteOffset = 50;
		}
		for (int smpCnt = 0; smpCnt < ecNr.arrayLine_.length; smpCnt++) {
			if (ecNr.arrayLine_[smpCnt] != 0.0D) {
				float a = (float) ecNr.arrayLine_[smpCnt];
				float b = (float) (this.sums.arrayLine_[smpCnt] - a);
				float c = ecNr.sum_ - a;
				float d = this.sums.sum_ - a - b - c;
				this.label_ = new JLabel(String.valueOf(odds(a, b, c, d)));
				this.lframe.step("adding " + ecNr.arrayLine_[smpCnt]);
				if (this.includeRepseq_.isSelected()) {
					final int indexY = index;
					final int indexX = smpCnt;
					JMenuItem mItem = new JMenuItem("Export");
					menuPopup = new JPopupMenu();

					menuPopup.add(mItem);
					ActMatrixPane.this.setComponentPopupMenu(menuPopup);
					/*
					 * Popup menu that comes up when you right click any of the ECs when
					 * the "include sequence id" option in selected
					 */
					mItem.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e) {
							EcNr ecTmp = new EcNr(
									((Line) ActMatrixPane.this.ecMatrix_
											.get(popupIndexY)).getEc_());
							ecTmp.amount_ = ((int) ((Line) ActMatrixPane.this.ecMatrix_
									.get(popupIndexY)).arrayLine_[indexX]);
							ArrayList<ConvertStat> reps = new ArrayList();
							for (int statsCnt = 0; statsCnt < ((Sample) Project.samples_
									.get(popupIndexX)).conversions_.size(); statsCnt++) {
								String test = (((ConvertStat) ((Sample) Project.samples_
										.get(popupIndexX)).conversions_
										.get(statsCnt)).getDesc_());
								if ((ecTmp.name_
										.contentEquals(((ConvertStat) ((Sample) Project.samples_
												.get(popupIndexX)).conversions_
												.get(statsCnt)).getEcNr_()))
										&& !test.contains("\t")) {
									reps.add((ConvertStat) ((Sample) Project.samples_
											.get(popupIndexX)).conversions_
											.get(statsCnt));
								}
							}

							String test = "";
							String test2 = "";
							for (int i = reps.size() - 1; i >= 0; i--) {
								if ((reps.get(i) == null)) {
								} else {
									test = ((ConvertStat) reps.get(i))
											.getDesc_();
									// System.out.println("1  "+test);
									if (test.contains("\t")) {
										reps.set(i, null);
									} else {
										// innerloop:
										for (int j = i - 1; j >= 0; j--) {
											if ((reps.get(j) == null)) {
											} else {
												test2 = ((ConvertStat) reps
														.get(j)).getDesc_();
												// System.out.println("2  "+
												// test2);
												if (test.contains(test2)) {
													reps.set(j, null);
												}
											}
										}
									}
								}
							}
							for (int i = reps.size() - 1; i >= 0; i--) {
								if (reps.get(i) == null) {
									reps.remove(i);
								}
							}

							String sampName = ((Sample) Project.samples_
									.get(popupIndexX)).name_;
							ExportReps(reps, ecTmp, sampName);
						}
					});
					/*
					 * Opens up the window when you left click the ECs with "include sequence ids"
					 * selected
					 */
					this.label_.addMouseListener(new MouseListener()
							{
								public void mouseClicked(MouseEvent e) {
									if (SwingUtilities.isRightMouseButton(e)
											|| e.isControlDown()) {
										System.out
												.println("Right Button Pressed");
										ActMatrixPane.this.menuPopup.show(
												e.getComponent(), e.getX(),
												e.getY());
										popupIndexY = indexY;
										popupIndexX = indexX;
									} else {
										EcNr ecTmp = new EcNr(
												((Line) ActMatrixPane.this.ecMatrix_
														.get(indexY)).getEc_());
										ecTmp.amount_ = ((int) ((Line) ActMatrixPane.this.ecMatrix_
												.get(indexY)).arrayLine_[indexX]);
										ArrayList<ConvertStat> reps = new ArrayList();
										for (int statsCnt = 0; statsCnt < ((Sample) Project.samples_
												.get(indexX)).conversions_
												.size(); statsCnt++) {
											String test = (((ConvertStat) ((Sample) Project.samples_
													.get(indexX)).conversions_
													.get(statsCnt)).getDesc_());
											if ((ecTmp.name_
													.contentEquals(((ConvertStat) ((Sample) Project.samples_
															.get(indexX)).conversions_
															.get(statsCnt))
															.getEcNr_()))
													&& !test.contains("\t")) {
												reps.add((ConvertStat) ((Sample) Project.samples_
														.get(indexX)).conversions_
														.get(statsCnt));
											}
										}

										String test = "";
										String test2 = "";
										for (int i = reps.size() - 1; i >= 0; i--) {
											if ((reps.get(i) == null)) {
											} else {
												test = ((ConvertStat) reps
														.get(i)).getDesc_();
												// System.out.println("1  "+test);
												if (test.contains("\t")) {
													reps.set(i, null);
												} else {
													// innerloop:
													for (int j = i - 1; j >= 0; j--) {
														if ((reps.get(j) == null)) {
														} else {
															test2 = ((ConvertStat) reps
																	.get(j))
																	.getDesc_();
															// System.out.println("2  "+
															// test2);
															if (test.contains(test2)) {
																reps.set(j,
																		null);
															}
														}
													}
												}
											}
										}
										for (int i = reps.size() - 1; i >= 0; i--) {
											if (reps.get(i) == null) {
												reps.remove(i);
											}
										}

										String sampName = ((Sample) Project.samples_
												.get(indexX)).name_;
										RepseqFrame repFrame = new RepseqFrame(
												reps, ecTmp, sampName);
									}
								}

								public void mouseEntered(MouseEvent e) {
								}

								public void mouseExited(MouseEvent e) {
								}

								public void mousePressed(MouseEvent e) {
								}

								public void mouseReleased(MouseEvent e) {
								}
							});
				}
			} else {
				this.label_ = new JLabel("0");
			}
			this.label_.setBounds(50 + (smpCnt + 1) * 130, uncompleteOffset
					+ 50 + index * 15, 130, 15);
			this.label_.setVisible(true);
			this.label_.setLayout(null);
			this.displayP_.add(this.label_);
		}
		if (ecNr.sum_ != 0) {
			this.label_ = new JLabel(String.valueOf(ecNr.sum_));
			this.lframe.step("adding " + ecNr.sum_);
		} else {
			this.label_ = new JLabel("0");
		}
		this.label_.setBounds(50 + (ecNr.arrayLine_.length + 1) * 130,
				uncompleteOffset + 50 + index * 15, 130, 15);
		this.label_.setVisible(true);
		this.label_.setLayout(null);
		this.displayP_.add(this.label_);
	}

	private void showValues(Line ecNr, int index) {// Prints the ec matrix
		if (ecNr.isSumline_()) {
			addSumLineVals(ecNr, index);
			return;
		}
		int uncompleteOffset = 0;
		if ((!ecNr.getEc_().isCompleteEc()) && (!ecNr.isMappedSums_())
				&& (!ecNr.isUnMappedSums_())) {
			uncompleteOffset = 50;
		}
		for (int smpCnt = 0; smpCnt < ecNr.arrayLine_.length; smpCnt++) {
			if (ecNr.arrayLine_[smpCnt] != 0.0D) {
				this.label_ = new JLabel(
						String.valueOf((int) ecNr.arrayLine_[smpCnt]));
				this.lframe.step("adding " + ecNr.arrayLine_[smpCnt]);
				if (this.includeRepseq_.isSelected()) {
					final int indexY = index;
					final int indexX = smpCnt;

					JMenuItem mItem = new JMenuItem("Export");
					menuPopup = new JPopupMenu();

					menuPopup.add(mItem);
					ActMatrixPane.this.setComponentPopupMenu(menuPopup);

					mItem.addActionListener(new ActionListener()// this is the
																// little popup
																// menu that
																// comes up when
																// you right
																// click any of
																// the ECs when
																// the
																// "include repseqs"
																// option is
																// selected
					{
						public void actionPerformed(ActionEvent e) {
							EcNr ecTmp = new EcNr(
									((Line) ActMatrixPane.this.ecMatrix_
											.get(popupIndexY)).getEc_());
							ecTmp.amount_ = ((int) ((Line) ActMatrixPane.this.ecMatrix_
									.get(popupIndexY)).arrayLine_[indexX]);
							ArrayList<ConvertStat> reps = new ArrayList();
							for (int statsCnt = 0; statsCnt < ((Sample) Project.samples_
									.get(popupIndexX)).conversions_.size(); statsCnt++) {
								String test = (((ConvertStat) ((Sample) Project.samples_
										.get(popupIndexX)).conversions_
										.get(statsCnt)).getDesc_());
								if ((ecTmp.name_
										.contentEquals(((ConvertStat) ((Sample) Project.samples_
												.get(popupIndexX)).conversions_
												.get(statsCnt)).getEcNr_()))
										&& !test.contains("\t")) {
									reps.add((ConvertStat) ((Sample) Project.samples_
											.get(popupIndexX)).conversions_
											.get(statsCnt));
								}
							}

							String test = "";
							String test2 = "";
							for (int i = reps.size() - 1; i >= 0; i--) {
								if ((reps.get(i) == null)) {
								} else {
									test = ((ConvertStat) reps.get(i))
											.getDesc_();
									// System.out.println("1  "+test);
									if (test.contains("\t")) {
										reps.set(i, null);
									} else {
										// innerloop:
										for (int j = i - 1; j >= 0; j--) {
											if ((reps.get(j) == null)) {
											} else {
												test2 = ((ConvertStat) reps
														.get(j)).getDesc_();
												// System.out.println("2  "+
												// test2);
												if (test.contains(test2)) {
													reps.set(j, null);
												}
											}
										}
									}
								}
							}
							for (int i = reps.size() - 1; i >= 0; i--) {
								if (reps.get(i) == null) {
									reps.remove(i);
								}
							}

							String sampName = ((Sample) Project.samples_
									.get(popupIndexX)).name_;
							ExportReps(reps, ecTmp, sampName);
						}
					});
					/*
					 * Opens up the window when you left click the ECs with the
					 * "include sequence ids" selected
					 */
					this.label_.addMouseListener(new MouseListener()
							{
								public void mouseClicked(MouseEvent e) {
									if (SwingUtilities.isRightMouseButton(e)
											|| e.isControlDown()) {
										System.out
												.println("Right Button Pressed");
										ActMatrixPane.this.menuPopup.show(
												e.getComponent(), e.getX(),
												e.getY());
										popupIndexY = indexY;
										popupIndexX = indexX;
									} else {
										EcNr ecTmp = new EcNr(
												((Line) ActMatrixPane.this.ecMatrix_
														.get(indexY)).getEc_());
										ecTmp.amount_ = ((int) ((Line) ActMatrixPane.this.ecMatrix_
												.get(indexY)).arrayLine_[indexX]);
										ArrayList<ConvertStat> reps = new ArrayList();
										for (int statsCnt = 0; statsCnt < ((Sample) Project.samples_
												.get(indexX)).conversions_
												.size(); statsCnt++) {
											if (ecTmp.name_
													.contentEquals(((ConvertStat) ((Sample) Project.samples_
															.get(indexX)).conversions_
															.get(statsCnt))
															.getEcNr_())) {
												reps.add((ConvertStat) ((Sample) Project.samples_
														.get(indexX)).conversions_
														.get(statsCnt));
											}
										}

										String test = "";
										String test2 = "";
										for (int i = reps.size() - 1; i >= 0; i--) {
											if ((reps.get(i) == null)) {
											} else {
												test = ((ConvertStat) reps
														.get(i)).getDesc_();
												// System.out.println("1  "+test);
												if (test.contains("\t")) {
													reps.set(i, null);
												} else {
													// innerloop:
													for (int j = i - 1; j >= 0; j--) {
														if ((reps.get(j) == null)) {
														} else {
															test2 = ((ConvertStat) reps
																	.get(j))
																	.getDesc_();
															// System.out.println("2  "+
															// test2);
															if (test.contains(test2)) {
																reps.set(j,
																		null);
															}
														}
													}
												}
											}
										}
										for (int i = reps.size() - 1; i >= 0; i--) {
											if (reps.get(i) == null) {
												reps.remove(i);
											}
										}

										String sampName = ((Sample) Project.samples_
												.get(indexX)).name_;
										RepseqFrame repFrame = new RepseqFrame(
												reps, ecTmp, sampName);
									}
								}

								public void mouseEntered(MouseEvent e) {
								}

								public void mouseExited(MouseEvent e) {
								}

								public void mousePressed(MouseEvent e) {
								}

								public void mouseReleased(MouseEvent e) {
								}
							});
				}
			} else {
				this.label_ = new JLabel("0");
			}
			this.label_.setBounds(50 + (smpCnt + 1) * 130, uncompleteOffset
					+ 50 + index * 15, 130, 15);
			this.label_.setVisible(true);
			this.label_.setLayout(null);
			this.displayP_.add(this.label_);
		}
		if (ecNr.sum_ != 0) {
			this.label_ = new JLabel(String.valueOf(ecNr.sum_));
			this.lframe.step("adding " + ecNr.sum_);
		} else {
			this.label_ = new JLabel("0");
		}
		this.label_.setBounds(50 + (ecNr.arrayLine_.length + 1) * 130,
				uncompleteOffset + 50 + index * 15, 130, 15);
		this.label_.setVisible(true);
		this.label_.setLayout(null);
		this.displayP_.add(this.label_);
	}
	//exports the sequence ids of a particular EC to RepSeqIDs
	public void ExportReps(ArrayList<ConvertStat> reps, EcNr ecNr,
			String sampName) {
		String text = "";
		String test = "";
		System.out.println("Reps:" + reps.size());
		for (int repCnt = 0; repCnt < reps.size(); repCnt++) {
			int amount = ((ConvertStat) reps.get(repCnt)).getEcAmount_();
			if (((ConvertStat) reps.get(repCnt)).getPfamToEcAmount_() > amount) {
				amount = ((ConvertStat) reps.get(repCnt)).getPfamToEcAmount_();
			}
			// test=((ConvertStat)reps.get(repCnt)).getDesc_();
			// if(!test.contains("\t")){
			text = text + ((ConvertStat) reps.get(repCnt)).getDesc_();
			text = text + "\n";
			// }
		}
		// System.out.println("Text:\n"+text);
		try {
			String sampleName;
			if (sampName.contains(".out")) {
				sampleName = sampName.replace(".out", "");
			} else {
				sampleName = sampName;
			}
			File file = new File(basePath_ + "RepSeqIDs" + File.separator
					+ sampleName + "-" + ecNr.name_ + ".txt");
			// file.getParentFile().mkdirs();
			PrintWriter printWriter = new PrintWriter(file);
			printWriter.println("" + text);
			// System.out.println("Text:\n"+text);
			printWriter.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	//adds the sum of a line in order to be able to print the line sum
	private void addSumLineVals(Line ecNr, int index) {
		int uncompleteOffset = 0;
		if ((!ecNr.isMappedSums_()) && (!ecNr.isUnMappedSums_())) {
			uncompleteOffset = 50;
		}
		for (int smpCnt = 0; smpCnt < ecNr.arrayLine_.length; smpCnt++) {
			if (ecNr.arrayLine_[smpCnt] != 0.0D) {
				this.label_ = new JLabel(
						String.valueOf((int) ecNr.arrayLine_[smpCnt]));
				this.lframe.step("adding " + ecNr.arrayLine_[smpCnt]);
			} else {
				this.label_ = new JLabel("0");
			}
			this.label_.setBounds(50 + (smpCnt + 1) * 130, uncompleteOffset
					+ 50 + index * 15, 130, 15);
			this.label_.setVisible(true);
			this.label_.setLayout(null);
			this.displayP_.add(this.label_);
		}
		if (ecNr.sum_ != 0) {
			this.label_ = new JLabel(String.valueOf(ecNr.sum_));
			this.lframe.step("adding " + ecNr.sum_);
		} else {
			this.label_ = new JLabel("0");
		}
		this.label_.setBounds(50 + (ecNr.arrayLine_.length + 1) * 130,
				uncompleteOffset + 50 + index * 15, 130, 15);
		this.label_.setVisible(true);
		this.label_.setLayout(null);
		this.displayP_.add(this.label_);
	}
	//adds all of the EC name buttons on the left hand side of the ec matrix
	//ADD EC BUTTON
	private void addEcButton(Line ecNr, int index) {
		this.lframe.bigStep("Adding ecButtons");
		if (!ecNr.isSumline_()) {
			ecNr.getEc_().addStats();
			this.names_ = new JButton(ecNr.getEc_().name_
					+ ecNr.getEc_().nameSuppl());
			this.names_.setName(ecNr.getEc_().name_);
			ec_list.add(ecNr.getEc_().name_);
			//System.out.println(ecNr.getEc_().name_
					//+ ecNr.getEc_().nameSuppl());
			int uncompleteOffset = 0;
			if (!ecNr.getEc_().isCompleteEc()) {
				uncompleteOffset = 50;
			}
			this.names_.setBounds(50, uncompleteOffset + 50 + index * 15, 130,
					15);
			this.names_.setVisible(true);
			this.names_.setLayout(null);
			this.names_.setForeground(Project.getFontColor_());
			this.lframe.step(this.names_.getText());
			//System.out.println(names_.getText());
			final int i = index;
			ecNr.getEc_().amount_ = ecNr.sum_;
			
			ActMatrixPane.this.setComponentPopupMenu(menuPopup);
			this.names_.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PwInfoFrame frame = new PwInfoFrame(
							((Line) ActMatrixPane.this.ecMatrix_.get(i))
									.getEc_(), ActMatrixPane.this.actProj_,
							Project.overall_);
					
				}
			});
			if (this.includeRepseq_.isSelected()) {
				JMenuItem mItem2 = new JMenuItem("Without finding Lowest Common Ancestor");
				JMenuItem mItem3 = new JMenuItem("Without finding Lowest Common Ancestor");
				JMenuItem mItem4 = new JMenuItem("With finding Lowest Common Ancestor");
				JMenuItem mItem5 = new JMenuItem("With finding Lowest Common Ancestor");
				ecMenuPopup = new JPopupMenu();
				JMenu submenu = new JMenu("Export all Sequences to one file");
			    submenu.add(mItem2);
			    submenu.add(mItem4);
			    
			    
			    JMenu submenu2 = new JMenu("Export all Sequences to individual files");
			    submenu2.add(mItem3);
			    submenu2.add(mItem5);
			    
			    ecMenuPopup.add(submenu);
			    ecMenuPopup.add(submenu2);
				
				mItem2.addActionListener(new ActionListener(){
					//If the user clicks on the "Export all Sequences" in the popup menu, sets the exportAll boolean to true
					//sends the buttons EC number into cmdExportSequences to be handled like a command line option
					@Override
					public void actionPerformed(ActionEvent e) {
						exportAll = true;
						//System.out.println(buttonName);
						lframe = new Loadingframe(); // opens the loading frame
						lframe.bigStep("Exporting Sequences..");
						lframe.step(buttonName); 
						cmdExportSequences(buttonName,sampleName, exportAll, findLca);
						exportAll = false;
						Loadingframe.close();
						
					}
					
				});
				mItem3.addActionListener(new ActionListener(){
					//If the user clicks on the "Export all Sequences" in the popup menu, sets the exportAll boolean to true
					//sends the buttons EC number into cmdExportSequences to be handled like a command line option
					@Override
					public void actionPerformed(ActionEvent e) {
						exportAll = false;
						//System.out.println(buttonName);
						lframe = new Loadingframe(); // opens the loading frame
						lframe.bigStep("Exporting Sequences..");
						lframe.step(buttonName); 
						cmdExportSequences(buttonName,sampleName, exportAll, findLca);
						Loadingframe.close();
						
					}
					
				});
				mItem4.addActionListener(new ActionListener(){
					//If the user clicks on the "Export all Sequences" in the popup menu, sets the exportAll boolean to true
					//sends the buttons EC number into cmdExportSequences to be handled like a command line option
					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("One file");
						exportAll = true;
						findLca = true;
						lframe = new Loadingframe(); // opens the loading frame
						lframe.bigStep("Exporting Sequences/Finding Lca..");
						lframe.step(buttonName); 
						LinkedHashMap<String,String> seq_for_lca;
						seq_for_lca = cmdExportSequences(buttonName,sampleName, exportAll, findLca);
						String file_name = buttonName+"-";
						MetaProteomicAnalysis meta = new MetaProteomicAnalysis();
						meta.getTrypticPeptideAnaysis(meta.readFasta(seq_for_lca,file_name), true);
						exportAll = false;
						findLca = false;
						Loadingframe.close();
					}
					
				});
				mItem5.addActionListener(new ActionListener(){
					//If the user clicks on the "Export all Sequences" in the popup menu, sets the exportAll boolean to true
					//sends the buttons EC number into cmdExportSequences to be handled like a command line option
					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("Many File");
						exportAll = false;
						findLca = true;
						lframe = new Loadingframe(); // opens the loading frame
						lframe.bigStep("Exporting Sequences/Finding Lca.."); 
						lframe.step(buttonName);
						cmdExportSequences(buttonName,sampleName, exportAll,findLca);
						findLca = false;
						Loadingframe.close();
					}
					
				});
				//When someone left clicks on the EC number it opens a popup menu containing the option to
				//export all sequences for that EC number
				this.names_.addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent e) {
						if (SwingUtilities.isRightMouseButton(e)
								|| e.isControlDown()) {
							ActMatrixPane.this.ecMenuPopup.show(e.getComponent(),
									e.getX(), e.getY());
							buttonName = e.getComponent().getName();
					
						}
					}
					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub
					}
					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub
					}
					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub
					}
					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub

					}
				});
			}
		} else {
			int uncompleteOffset = 0;
			if (ecNr.isMappedSums_()) {
				this.names_ = new JButton("MappedECs");
			} else if (ecNr.isUnMappedSums_()) {
				this.names_ = new JButton("UnMappedECs");
			} else if (ecNr.isincompleteSums_()) {
				this.names_ = new JButton("InCompleteECs");
				uncompleteOffset = 50;
			} else {
				this.names_ = new JButton("All ECs");
				uncompleteOffset = 50;
			}
			this.names_.setBounds(50, uncompleteOffset + 50 + index * 15, 130,
					15);
			this.names_.setVisible(true);
			this.names_.setLayout(null);
			this.names_.setForeground(Project.getFontColor_());
			this.lframe.step(this.names_.getText());
		}
		this.displayP_.add(this.names_);
	}

	private void switchOptionsMode() {// Switches the option mode
		if (this.showOptions_.isSelected()) {
			this.useOddsrat_.setVisible(true);
			this.bySumcheck_.setVisible(true);
			this.moveUnmappedToEnd.setVisible(true);
			this.resort_.setVisible(true);
			this.maxVisField_.setVisible(true);
			this.includeRepseq_.setVisible(true);
		} else {
			this.useOddsrat_.setVisible(false);
			this.bySumcheck_.setVisible(false);
			this.moveUnmappedToEnd.setVisible(false);
			this.resort_.setVisible(false);
			this.maxVisField_.setVisible(false);
			this.includeRepseq_.setVisible(false);
		}
	}

	// This method is no longer implemented in Bubble Sort, I just left the name
	// incase unforeseen parts of the program were dependant upon it
	// changed method name from sortEcsbyNameQuickSort() to
	// sortEcsbyNameBubble()
	private void sortEcsbyNameQuickSort() {
		if (this.sortedEc) {
			return;
		}
		this.lframe.bigStep("Sorting ECs");
		System.out.println("Sorting ECs");
		quicksortNames(0, this.ecMatrix_.size() - 1);
		this.ecMatrix_ = removeDuplicates();
		System.out.println("Done Sorting");
		if (this.moveUnmappedToEnd != null) {
			if (this.moveUnmappedToEnd.isSelected()) {
				unmappedMover();
			}
		} else {
			unmappedMover();
		}
		unCompleteMover();
		Loadingframe.close();
	}

	private void quicksortNames(int low, int high) {
		int i = low, j = high;
		String pivot = this.ecMatrix_.get(low + (high - low) / 2).getEc_().name_;
		while (i <= j) {
			while (this.ecMatrix_.get(i).getEc_().name_.compareTo(pivot) < 0) {
				i++;
			}
			while (this.ecMatrix_.get(j).getEc_().name_.compareTo(pivot) > 0) {
				j--;
			}
			if (i <= j) {
				switchEcs(i, j);
				i++;
				j--;
			}
		}
		if (low < j)
			quicksortNames(low, j);
		if (i < high)
			quicksortNames(i, high);
	}

	private ArrayList<Line> removeDuplicates() {
		ArrayList<Line> tempLine = new ArrayList<Line>();
		ArrayList<String> tempName = new ArrayList<String>();
		for (int i = 0; i < this.ecMatrix_.size(); i++) {
			if (!tempName.contains(this.ecMatrix_.get(i).getEc_().name_)) {
				tempLine.add(this.ecMatrix_.get(i));
				tempName.add(this.ecMatrix_.get(i).getEc_().name_);
			}
		}
		return tempLine;
	}

	private void unmappedMover() {
		int unmappedCnt = 0;
		for (int ecCnt1 = 0; ecCnt1 < this.ecMatrix_.size(); ecCnt1++) {
			((Line) this.ecMatrix_.get(ecCnt1)).getEc_().addStats();
			if ((((Line) this.ecMatrix_.get(ecCnt1)).getEc_().unmapped)
					&& (ecCnt1 < this.ecMatrix_.size() - unmappedCnt)) {
				moveToEnd(ecCnt1);
				unmappedCnt++;
				ecCnt1--;
			}
		}
	}

	private void unCompleteMover() {
		int unCompleteCnt = 0;
		for (int ecCnt1 = 0; ecCnt1 < this.ecMatrix_.size(); ecCnt1++) {
			if ((!((Line) this.ecMatrix_.get(ecCnt1)).getEc_().isCompleteEc())
					&& (ecCnt1 < this.ecMatrix_.size() - unCompleteCnt)) {
				moveToEnd(ecCnt1);
				unCompleteCnt++;
				ecCnt1--;
			}
		}
	}

	private void sortEcsbySumBubble() {
		System.out.println("Sum Bubble");
		if (this.sortedEc) {
			return;
		}
		this.lframe.bigStep("Sorting ECs");
		boolean changed = true;
		double sum1 = 0.0D;
		double sum2 = 0.0D;
		if (!this.sortedEc) {
			int ecCnt1 = 0;
			while (changed && (ecCnt1 < this.ecMatrix_.size())) {
				changed = false;
				sum1 = 0.0D;
				if (this.selectedSampIndex_ < 0) {
					sum1 = ((Line) this.ecMatrix_.get(ecCnt1)).sum_;
				} else {
					sum1 = ((Line) this.ecMatrix_.get(ecCnt1))
							.getEntry(this.selectedSampIndex_);
				}
				sum2 = 0.0D;
				for (int ecCnt2 = ecCnt1 + 1; ecCnt2 < this.ecMatrix_.size(); ecCnt2++) {
					sum2 = 0.0D;
					this.lframe.step("comparing");
					if (this.selectedSampIndex_ < 0) {
						sum2 = ((Line) this.ecMatrix_.get(ecCnt2)).sum_;
					} else {
						sum2 = ((Line) this.ecMatrix_.get(ecCnt2))
								.getEntry(this.selectedSampIndex_);
					}
					if (sum1 >= sum2) {
						break;
					}
					switchEcs(ecCnt1, ecCnt2);
					ecCnt1++;
					ecCnt2++;
					changed = true;
					this.lframe.step("switching");
				}
				ecCnt1++;
			}
			this.sortedEc = true;
		}
		if (this.moveUnmappedToEnd.isSelected()) {
			unmappedMover();
		}
		unCompleteMover();
	}

	private void moveToEnd(int index) {
		int tmp = index;
		while (tmp < this.ecMatrix_.size() - 1) {
			switchEcs(tmp, tmp + 1);
			tmp++;
		}
	}
	//exports the whole matrix to the input path
	public void exportMat(String path, boolean inCsf) {
		String separator = "\t";
		String pathway = "";
		if (inCsf) {
			separator = "\t";
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			out.write("EcActivity" + separator);
			out.newLine();
			out.newLine();
			for (int smpCnt = 0; smpCnt < Project.samples_.size(); smpCnt++) {
				if (((Sample) Project.samples_.get(smpCnt)).inUse) {
					// out.write(separator);
					out.write(((Sample) Project.samples_.get(smpCnt)).name_
							+ separator);
					//System.out.println(((Sample) Project.samples_.get(smpCnt)).name_);
				}
			}
			out.newLine();
			for (int y = 0; y < this.ecMatrix_.size(); y++) {
				Line line = (Line) this.ecMatrix_.get(y);
				if ((!line.getEc_().isCompleteEc())
						&& (!this.dispIncomplete_.isSelected())) {
					break;
				}
				//adds the first pathway name associated with that EC number to the file
				if (proc_.getPathwayEc(line.getEc_().name_) != null) {
					pathway = proc_.getPathwayName(proc_.getPathwayEc(line
							.getEc_().name_));
				} else {
					pathway = "unmapped";
				}
				out.write(pathway + separator);
				out.write(line.getEc_().name_ + line.getEc_().nameSuppl()
						+ separator);
				for (int x = 0; x < line.getArrayLine_().length; x++) {
					if (this.useOddsrat_.isSelected()) {
						float a = (float) line.arrayLine_[x];
						float b = (float) (this.sums.arrayLine_[x] - a);
						float c = line.sum_ - a;
						float d = this.sums.sum_ - a - b - c;
						out.write(odds(a, b, c, d) + separator);
					} else {
						out.write(line.getArrayLine_()[x] + separator);
					}
				}
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			File f = new File(path);
			f.mkdirs();
			if (!path.endsWith(".txt")) {
				Calendar cal = Calendar.getInstance();

				int day = cal.get(5);
				int month = cal.get(2) + 1;
				int year = cal.get(1);

				String date = day + "." + month + "." + year;
				path = path + File.separator + "EcActMat" + "."
						+ Project.workpath_ + "." + date;
			}
			String tmpPath = path + ".txt";
			File file1 = new File(tmpPath);
			if (file1.exists() && !file1.isDirectory()) {
				int i = 1;
				while ("Pigs" != "Fly") {// loop forever
					tmpPath = path + "(" + i + ")" + ".txt";
					File file2 = new File(tmpPath);
					if (file2.exists() && !file2.isDirectory()) {
						i++;
						continue;
					} else {
						path = path + "(" + i + ")";
						break;
					}
				}
			}
			path = path + ".txt";
			exportMat(path, inCsf);
		}
	}

	private void quicksortSum() {
		this.lframe.bigStep("Sorting ECs");
		System.out.println("Sorting ECs by sum");
		quicksort(0, this.ecMatrix_.size() - 1);
		reverseMatrix();
		this.ecMatrix_ = removeDuplicates();
		System.out.println("Done Sorting");
		if (this.moveUnmappedToEnd != null) {
			if (this.moveUnmappedToEnd.isSelected()) {
				unmappedMover();
			}
		} else {
			unmappedMover();
		}

		unCompleteMover();
		Loadingframe.close();
	}

	private void quicksort(int low, int high) {
		int i = low, j = high;
		int pivot = this.ecMatrix_.get(high - 1).sum_;
		while (i <= j) {
			while (this.ecMatrix_.get(i).sum_ < pivot) {
				i++;
			}
			while (this.ecMatrix_.get(j).sum_ > pivot) {
				j--;
			}
			if (i <= j) {
				switchEcs(i, j);
				i++;
				j--;
			}
		}
		if (low < j)
			quicksort(low, j);
		if (i < high)
			quicksort(i, high);
	}

	private void reverseMatrix() {
		int j = 0;
		for (int i = ecMatrix_.size() - 1; i > j; i--) {
			switchEcs(i, j);
			j++;
		}
	}

	private void switchEcs(int index1, int index2) {
		Line line1 = (Line) this.ecMatrix_.get(index1);
		Line line2 = (Line) this.ecMatrix_.get(index2);

		this.ecMatrix_.set(index1, line2);
		this.ecMatrix_.set(index2, line1);
	}

	private float odds(float a, float b, float c, float d) {
		if (a == 0.0F) {
			return -1.0F;
		}
		if (b == 0.0F) {
			c = 0.001F;
		}
		if (c == 0.0F) {
			b = 0.001F;
		}
		if (d == 0.0F) {
			return -2.0F;
		}
		float ret = a / b / (c / d);

		return ret;
	}

	private void setSelectedEc() {
		for (int x = 0; x < Project.samples_.size(); x++) {
			if ((((Sample) Project.samples_.get(x)).inUse)
					&& (((Sample) Project.samples_.get(x)).onoff)) {
				ArrayList<EcWithPathway> actSample = ((Sample) Project.samples_
						.get(x)).ecs_;
				for (int ecCnt = 0; ecCnt < actSample.size(); ecCnt++) {
					boolean isSelected = false;
					EcWithPathway actEc = (EcWithPathway) actSample.get(ecCnt);
					for (int pwCnt = 0; pwCnt < actEc.pathways_.size(); pwCnt++) {
						if (this.proc_.getPathway(
								((Pathway) actEc.pathways_.get(pwCnt)).id_)
								.isSelected()) {
							isSelected = true;
						}
					}
					if (actEc.pathways_.size() == 0) {
						isSelected = true;
					}
					if (!isSelected) {
						actEc.isSelected_ = false;
					} else {
						actEc.isSelected_ = true;
					}
				}
			}
		}
	}
	//exports all sequence IDs for all samples given an EC name via cmdPrompt
	public void cmdExportRepseqs(String ecName) {
		EcNr ecTmp;

		for (int i = 0; i < this.ecMatrix_.size(); i++) {
			// System.out.println(this.ecMatrix_.get(i).getEc_().name_);
			if (ecName.contains(this.ecMatrix_.get(i).getEc_().name_)) {
				ecTmp = new EcNr(
						((Line) ActMatrixPane.this.ecMatrix_.get(i)).getEc_());
				for (int smpCnt = 0; smpCnt < ecMatrix_.get(i).arrayLine_.length; smpCnt++) {
					ecTmp.amount_ = ((int) ((Line) ActMatrixPane.this.ecMatrix_
							.get(i)).arrayLine_[smpCnt]);
					ArrayList<ConvertStat> reps = new ArrayList();
					for (int statsCnt = 0; statsCnt < ((Sample) Project.samples_
							.get(smpCnt)).conversions_.size(); statsCnt++) {
						String test = (((ConvertStat) ((Sample) Project.samples_
								.get(smpCnt)).conversions_.get(statsCnt))
								.getDesc_());
						if ((ecTmp.name_
								.contentEquals(((ConvertStat) ((Sample) Project.samples_
										.get(smpCnt)).conversions_
										.get(statsCnt)).getEcNr_()))
								&& !test.contains("\t")) {
							reps.add((ConvertStat) ((Sample) Project.samples_
									.get(smpCnt)).conversions_.get(statsCnt));
						}
					}
					String test = "";
					String test2 = "";
					for (int j = reps.size() - 1; j >= 0; j--) {
						if ((reps.get(j) == null)) {
						} else {
							test = ((ConvertStat) reps.get(j)).getDesc_();
							// System.out.println("1  "+test);
							if (test.contains("\t")) {
								reps.set(j, null);
							} else {
								// innerloop:
								for (int k = j - 1; k >= 0; k--) {
									if ((reps.get(k) == null)) {
									} else {
										test2 = ((ConvertStat) reps.get(k))
												.getDesc_();
										// System.out.println("2  "+ test2);
										if (test.contains(test2)) {
											reps.set(k, null);
										}
									}
								}
							}
						}
					}
					for (int j = reps.size() - 1; j >= 0; j--) {
						if (reps.get(j) == null) {
							reps.remove(j);
						}
					}
					String sampName = ((Sample) Project.samples_.get(smpCnt)).name_;
					if (reps.size() > 0) {
						ExportReps(reps, ecTmp, sampName);
					}
				}
			}
		}
	}
	/*
	 * Takes in command line calls to export sequences along with wheter or not to print all sequences samples into each EC number file
	 */
	public LinkedHashMap<String,String> cmdExportSequences(String ecName,String sampleName, boolean oneFile, boolean findLca) {
		System.out.println("cmdExport");
		int index;
		EcNr ecTmp;
		ArrayList<ConvertStat> reps;
		String sampName;
		LinkedHashMap<String,String> seq_for_lca = null;

		for (int i = 0; i < this.ecMatrix_.size(); i++) {
			//changed contains to equals!!
			if (ecName.equals(this.ecMatrix_.get(i).getEc_().name_)) {
				//System.out.println("cmd" + ecName);
				//System.out.println(this.ecMatrix_.get(i).getEc_().name_);
				ecTmp = new EcNr(((Line) ActMatrixPane.this.ecMatrix_.get(i)).getEc_());
				for (int smpCnt = 0; smpCnt < ecMatrix_.get(i).arrayLine_.length; smpCnt++) {
					ecTmp.amount_ = ((int) ((Line) ActMatrixPane.this.ecMatrix_
							.get(i)).arrayLine_[smpCnt]);
					reps = new ArrayList();
					for (int statsCnt = 0; statsCnt < ((Sample) Project.samples_
							.get(smpCnt)).conversions_.size(); statsCnt++) {
						String test = (((ConvertStat) ((Sample) Project.samples_
								.get(smpCnt)).conversions_.get(statsCnt))
								.getDesc_());
						if ((ecTmp.name_
								.contentEquals(((ConvertStat) ((Sample) Project.samples_
										.get(smpCnt)).conversions_
										.get(statsCnt)).getEcNr_()))
								&& !test.contains("\t")) {
							reps.add((ConvertStat) ((Sample) Project.samples_
									.get(smpCnt)).conversions_.get(statsCnt));
						}
					}
					String test = "";
					String test2 = "";
					for (int j = reps.size() - 1; j >= 0; j--) {
						if ((reps.get(j) == null)) {
						} else {
							test = ((ConvertStat) reps.get(j)).getDesc_();
							// System.out.println("1  "+test);
							if (test.contains("\t")) {
								reps.set(j, null);
							} else {
								// innerloop:
								for (int k = j - 1; k >= 0; k--) {
									if ((reps.get(k) == null)) {
									} else {
										test2 = ((ConvertStat) reps.get(k))
												.getDesc_();
										// System.out.println("2  "+ test2);
										if (test.contains(test2)) {
											reps.set(k, null);
										}
									}
								}
							}
						}
					}
					for (int j = reps.size() - 1; j >= 0; j--) {
						if (reps.get(j) == null) {
							reps.remove(j);
						}
					}
					sampName = ((Sample) Project.samples_.get(smpCnt)).name_;
						if(reps.size()>0&&((chosen_sample.equals("All Samples"))||(chosen_sample.equals("")))){
							//print all sequences per sample EC number 
							System.out.println("Working....\n");
							seq_for_lca = ExportSequences(reps, ecTmp, sampName, oneFile, findLca, seq_for_lca);
						}
						//used if trying to find the lca from the LCA page instead of the EC Matrix page
						else if(reps.size()>0 && chosen_sample.equals(sampName)){
							System.out.println("Working....\n");
							seq_for_lca = ExportSequences(reps, ecTmp, sampName, oneFile, findLca, seq_for_lca);
						}
						
				}
			}
		}
		return seq_for_lca;
	}
	/*
	 * exports sequences from EC numbers from a sample to their own named file.
	 */
	public LinkedHashMap<String,String> ExportSequences(ArrayList<ConvertStat> reps_, EcNr ecNr_,
			String sampName_, boolean oneFile, boolean findLca, LinkedHashMap<String,String> seqTmp) {
		String seqFilePath = "";
		String desc, protien, file_name;
		LinkedHashMap<String, String> seq_for_lca;
		if(seqTmp==null||oneFile==false||exportAll==false){
			seq_for_lca = new LinkedHashMap<String,String>();
		}
		else{
			seq_for_lca = seqTmp;
		}
		for (int i = 0; i < Project.samples_.size(); i++) {
			if (sampName_.equals(Project.samples_.get(i).name_)) {
				if (Project.samples_.get(i).getSequenceFile() != null
						&& !Project.samples_.get(i).getSequenceFile()
								.equals("")) {
					seqFilePath = Project.samples_.get(i).getSequenceFile();
				}
			}
		}
		if (seqFilePath != null && !seqFilePath.equals("")) {
			File seqFile = new File(seqFilePath);
			if (seqFile.exists() && !seqFile.isDirectory()) {
				LinkedHashMap<String, ProteinSequence> sequenceHash;
				try {
					sequenceHash = FastaReaderHelper.readFastaProteinSequence(seqFile);
					if (sequenceHash != null) {
						String text = ">";
						System.out.println("repCnt: " + reps_.size());
						for (int repCnt = 0; repCnt < reps_.size(); repCnt++) {
							if ((sequenceHash.get(((ConvertStat) reps_.get(repCnt)).getDesc_())) != null) {
								desc = ((ConvertStat) reps_.get(repCnt)).getDesc_();
								protien = (sequenceHash.get(((ConvertStat) reps_.get(repCnt)).getDesc_())).toString();
								if(oneFile){
									text = text + desc + " " + sampName_ + "\n" + protien;
									desc = desc + " " + sampName_;
								}
								else{
									text = text + desc + "\n" + protien;
									desc = desc + " " + sampName_;
								}
								//used to determine if a sample was picked in the "FindLca" page
								if(chosen_sample.equals("All Samples")||chosen_sample.equals("")){
									seq_for_lca.put(desc, protien);
								}
								else if(chosen_sample.equals(sampName_)){
									seq_for_lca.put(desc, protien);
								}
								// ensures that there is a ">" character in front of every new sample
								if (repCnt < reps_.size() - 1) {
									text = text + "\n>";
								}
								// Don't want the ">" character on the last newline with no sample
								else if (repCnt == reps_.size()) {
									text = text + "\n";
								}
							}
						}
						if(findLca == false && oneFile == false){
						try {
							String sampleName;
							if (sampName_.contains(".out")) {
								sampleName = sampName_.replace(".out", "");
							} else {
								sampleName = sampName_;
							}
							File file = new File(basePath_ + "Sequences"
									+ File.separator + sampleName + "-"
									+ ecNr_.name_ + "-Sequences" + ".txt");
							PrintWriter printWriter = new PrintWriter(file);
							if (text != null && text != "") {
								printWriter.println("" + text);
							} else {
								printWriter
										.println("No matching sequences in the file provided. ("
												+ sampName_ + ")");
							}
							printWriter.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						}
						else if(findLca == false && oneFile == true){
							try {
								String sampleName;
								if (sampName_.contains(".out")) {
									sampleName = sampName_.replace(".out", "");
								} else {
									sampleName = sampName_;
								}
								File file = new File(basePath_ + "Sequences"
										+ File.separator + ecNr_.name_ + "-Sequences" + ".txt");
								//This allows writing to the file of the same name to append to the file if created, creates file if not
								PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
								if (text != null && text != "") {
									printWriter.println("" + text);
								} else {
									printWriter
											.println("No matching sequences in the file provided. ("
													+ sampName_ + ")");
								}
								printWriter.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					} else {
						System.out
								.println("The sequence file is not in the fasta format. ("
										+ sampName_ + ")");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			} else {
				System.out
						.println("The sequence file associated with this sample ("
								+ sampName_ + ") does not exist");
			}
		} else {
			System.out
					.println("There is no sequence file associated with this sample ("
							+ sampName_ + ")");
		}
		//used to find the lowest common ancestor of all the samples for a particular ec in one table
		if(findLca == true && oneFile == false){
			if(chosen_sample.equals("All Samples")||chosen_sample.equals("")){
				file_name = sampName_ + "-" + ecNr_.name_;
			}
			else{
				file_name = chosen_sample + "-" + ecNr_.name_;
			}
			MetaProteomicAnalysis meta = new MetaProteomicAnalysis();
			meta.getTrypticPeptideAnaysis(meta.readFasta(seq_for_lca,file_name), true);
		}
		fileName = sampName_;
		return seq_for_lca;
	}

public boolean isDrawChart() {
	return drawChart;
}

public void setDrawChart(boolean drawChart) {
	this.drawChart = drawChart;
}

/**
 * Pops up whenever an exception occurs in a try/catch
 * @param strIN Error message to be displayed
 * 
 * @author Jennifer Terpstra
 */
private void warningFrame(String strIN) { 

	JOptionPane.showMessageDialog(null,strIN, 
		    "Warning", JOptionPane.WARNING_MESSAGE);
}

/**
 * Displays after a user exports tables or piechart with the file location, the location
 * can then be displayed to the user.
 * 
 * @param strIN Filepath towards the exported file
 * @param location Foler which the exported file is located
 * 
 * @author Jennifer Terpstra
 * @return 
 */
public void infoFrame(String strIN, final String location) {
	JFrame infoFrame = new JFrame();
	infoFrame.setBounds(200, 200, 800, 200);
	infoFrame.setLayout(null);
	infoFrame.setVisible(true);

	JPanel backP = new JPanel();
	backP.setBounds(0, 0, 800, 80);
	backP.setLayout(null);
	infoFrame.add(backP);

	JLabel label = new JLabel("File was saved at " + strIN);
	label.setBounds(20, 20, 800, 50);
	backP.add(label);
	
	JButton open = new JButton("Open file location");
	open.setBounds(250, 80, 225, 30);
	open.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			String path = "";
			try {
				path = new File("").getCanonicalPath();
			} catch (IOException e1) {
				
			}
			JFileChooser fChoose_ = new JFileChooser(path + File.separator
					+ location);
			fChoose_.setFileSelectionMode(0);
			fChoose_.setBounds(100, 100, 200, 20);
			fChoose_.setVisible(true);
			fChoose_.showOpenDialog(getParent());
		}
		
	});
	infoFrame.add(open);
}

}