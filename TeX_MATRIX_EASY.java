import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TeX_MATRIX_EASY extends JFrame{
	static int matrix_h=4;
	static int matrix_w=3;
	static JTextField[][] tx;
	static JPanel[] txp;
	static String[] menu= {"生成","列を増やす","列を減らす","行を増やす","行を減らす","CSV読込(UTF-8)","CSV読込(SHIFT-JIS)"};
	static JButton[] UI1=new JButton[7];
	static JPanel buttonpanel=new JPanel();
	static JScrollPane text_matrix;
	static JPanel content_p;
	static JFrame mainframe;
	static String[][] data;
	static JFrame subWindow;
	static JButton delete,invert;
	static JTextField caption;
	static JLabel captitle;
	static JPanel captionpanel;
	static int forcused_text_x=1;
	static int forcused_text_y=1;
	static Color forcused_color=new Color(0.75f,1.0f,1.0f);
	static int h=800;
	static int w=1200;
	static JPanel guide=new JPanel();
	static JLabel coord;
	static JPanel toolpanel=new JPanel();
	static String[][] inv;

	public TeX_MATRIX_EASY() {
		data=new String[matrix_h][matrix_w];
		init();
		tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
	}

	public static void main(String[] args) {
		new TeX_MATRIX_EASY();
	}

	public void init() {
		mainframe=null;
		subWindow=null;
		txp=null;
		buttonpanel=null;
		content_p=null;
		captionpanel=null;
		guide=null;
		toolpanel=null;
		captionpanel=null;

		buttonpanel=new JPanel();
		guide=new JPanel();
		toolpanel=new JPanel();

		subWindow=new JFrame("ツール");
		subWindow.setSize(400,200);
		delete=new JButton("表の内容をすべて消去する");
		toolpanel.setLayout(new BoxLayout(toolpanel, BoxLayout.Y_AXIS));
		invert=new JButton("表の転置");
		caption=new JTextField(20);
		captitle =new JLabel("キャプション");
		captionpanel = new JPanel();
		captionpanel.setLayout(new FlowLayout());
		captionpanel.add(captitle);
		captionpanel.add(caption);
		toolpanel.add(captionpanel);
		delete.addActionListener(new deletecontents());
		invert.addActionListener(new invertcontents());
		toolpanel.add(delete);
		toolpanel.add(invert);
		subWindow.add(toolpanel);
		subWindow.setDefaultCloseOperation(EXIT_ON_CLOSE);
		subWindow.setVisible(true);

		coord=new JLabel("("+forcused_text_x+" , "+forcused_text_y+")");
		guide.add(coord);
		mainframe=new JFrame();
		mainframe.setSize(w,h);
		mainframe.setTitle("TeX表ラクラク作成");
		mainframe.add(buttonpanel,BorderLayout.NORTH);
		content_p = new JPanel();
		buttonpanel.setLayout(new GridLayout(1,7));
		for(int i=0; i<7; i++) {
			UI1[i]=new JButton(menu[i]);
			buttonpanel.add(UI1[i]);
		}
		UI1[0].addActionListener(new Generation());		//表生成
		UI1[1].addActionListener(new AddColumn());		//列を増やす
		UI1[2].addActionListener(new DecColumn());		//列を減らす
		UI1[3].addActionListener(new AddRow());			//行を増やす
		UI1[4].addActionListener(new DecRow());			//行を減らす
		UI1[5].addActionListener(new LoadCSVU8());		//CSVを読み込み
		UI1[6].addActionListener(new LoadCSVSJ());		//CSVを読み込み

		tx=new JTextField[matrix_h+1][matrix_w+1];
		txp=new JPanel[matrix_h+1];
		for(int i=0; i<=matrix_h; i++) {
			txp[i]=new JPanel();
			txp[i].setLayout(new BoxLayout(txp[i], BoxLayout.X_AXIS));
			for(int j=0; j<=matrix_w; j++) {
				if(i==0) {
					if(j==0) {
						tx[i][j]=new JTextField("");
						tx[i][j].setFocusable(false);
						tx[i][j].setEditable(false);
					}
					else {
						tx[i][j]=new JTextField(""+j+"");
						tx[i][j].setHorizontalAlignment(JTextField.CENTER);
						tx[i][j].setEditable(false);
						tx[i][j].setFocusable(false);
					}
					tx[i][j].setPreferredSize(new Dimension(120,30));
				}
				else {
					if(j==0) {
						tx[i][j]=new JTextField(""+i+"");
						tx[i][j].setHorizontalAlignment(JTextField.RIGHT);
						tx[i][j].setEditable(false);
						tx[i][j].setFocusable(false);
					}
					else {
						tx[i][j]=new JTextField();
					}
					tx[i][j].setPreferredSize(new Dimension(190,30));
					tx[i][j].addFocusListener(new excel_like_perform());
					tx[i][j].addKeyListener(new excel_like_perform());
				}
				txp[i].add(tx[i][j]);
			}
		}
		text_matrix = new JScrollPane(content_p);
		content_p.setLayout(new BoxLayout(content_p, BoxLayout.Y_AXIS));
		mainframe.add(text_matrix,BorderLayout.CENTER);
		mainframe.add(guide,BorderLayout.SOUTH);
		for(int i=0; i<=matrix_h; i++) {
			this.revalidate();
			content_p.add(txp[i]);
			content_p.doLayout();
		}
		mainframe.setDefaultCloseOperation(EXIT_ON_CLOSE);
		mainframe.setVisible(true);
	}

	public void refresh(int old_matrix_h,int old_matrix_w) {
		String[][] newdata=new String[matrix_h+1][matrix_w+1];
		for(int i=0; i<matrix_h; i++) {
			for(int j=0; j<matrix_w; j++) {
				newdata[i][j]="";
			}
		}
		for(int i=0; i<Math.min(matrix_h, old_matrix_h); i++) {
			for(int j=0; j<Math.min(matrix_w, old_matrix_w); j++) {
				newdata[i][j]=data[i][j];
			}
		}
		data=newdata.clone();
		init();
		loadData();
		newdata=null;
	}

	public void OpenCSV() {
		init();
		loadData();
	}

	public void loadData() {
		for(int i=1; i<=matrix_h; i++) {
			for(int j=1; j<=matrix_w; j++) {
				tx[i][j].setText(data[i-1][j-1]);
			}
		}
	}
	public void dataSave() {
		for(int i=1; i<=matrix_h; i++) {
			for(int j=1; j<=matrix_w; j++) {
				data[i-1][j-1]=tx[i][j].getText();
			}
		}
	}

	class deletecontents implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			data=new String[matrix_h][matrix_w];
			mainframe.dispose();
			subWindow.dispose();
			refresh(matrix_h,matrix_w-1);	//いままでのmatrix	テキストボックス内の内容を保持するために使う
			forcused_text_y=1;
			forcused_text_x=1;
			tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
		}
	}

	class invertcontents implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			dataSave();
			inv=new String[matrix_w][matrix_h];
			for(int i=0; i<matrix_w; i++) {
				for(int j=0; j<matrix_h; j++) {
					inv[i][j]="";
					inv[i][j]=data[j][i];
				}
			}
			data=new String[matrix_w][matrix_h];
			for(int i=0; i<matrix_w; i++) {
				for(int j=0; j<matrix_h; j++) {
					data[i][j]=inv[i][j];
				}
			}
			int tmp=matrix_w;
			matrix_w=matrix_h;
			matrix_h=tmp;
			mainframe.dispose();
			subWindow.dispose();
			refresh(matrix_h,matrix_w);	//いままでのmatrix	テキストボックス内の内容を保持するために使う
			forcused_text_y=1;
			forcused_text_x=1;
			tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
		}
	}

	class excel_like_perform implements FocusListener,KeyListener{
		public void focusGained(FocusEvent e) {
			for(int i=1; i<=matrix_h; i++) {
				for(int j=1; j<=matrix_w; j++) {
					if(tx[i][j].isFocusOwner()) {
						forcused_text_y=i;
						forcused_text_x=j;
						tx[i][j].setBackground(forcused_color);
						JViewport view=text_matrix.getViewport();
						view.setViewPosition(new Point(Math.max(j*190-600,0),Math.max(i*30-400,0)));	//これだけでよかったんや…
					}
				}
			}
			coord.setText("("+forcused_text_x+" , "+forcused_text_y+")");
		}
		public void focusLost(FocusEvent e) {
			JTextField jt=(JTextField) e.getComponent();
			jt.setBackground(Color.WHITE);
		}
		public void keyTyped(KeyEvent e) {
			//none
		}
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode()==40) {		//Excelみたいに動く	下方向のキーならフォーカスを下に動かす
				if(forcused_text_y+1<=matrix_h) {
					forcused_text_y++;
					tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
				}
				else {
					forcused_text_y=1;
					tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
				}
			}
			else if(e.getKeyCode()==39) {		//Excelみたいに動く	右方向のキーならフォーカスを右に動かす
				if(forcused_text_x+1<=matrix_w) {
					forcused_text_x++;
					tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
				}
				else {
					forcused_text_x=1;
					tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
				}
			}
			else if(e.getKeyCode()==38) {		//Excelみたいに動く	上方向のキーならフォーカスを上に動かす
				if(forcused_text_y-1>=1) {
					forcused_text_y--;
					tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
				}
				else {
					forcused_text_y=matrix_h;
					tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
				}
			}
			else if(e.getKeyCode()==37) {		//Excelみたいに動く	左方向のキーならフォーカスを左に動かす
				if(forcused_text_x-1>=1) {
					forcused_text_x--;
					tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
				}
				else {
					forcused_text_x=matrix_w;
					tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
				}
			}
		}
		public void keyReleased(KeyEvent e) {
			//none
		}
	}

	class Generation implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			JFrame show=new JFrame("ソース");
			JTextArea txa=new JTextArea();
			txa.append("\\begin{table}[H]\n");
			StringBuilder sb=new StringBuilder();
			for(int i=0; i<matrix_w; i++) {
				sb.append("l");
			}
			txa.append("\\begin{center} \n");
			txa.append("\t \\begin{tabular}{"+sb.toString()+"}\\hline\n");
			for(int i=0; i<matrix_h; i++) {
				sb=new StringBuilder();
				sb.append("\t\t");
				for(int j=0; j<matrix_w; j++) {
					sb.append(tx[i+1][j+1].getText());
					if(j!=matrix_w-1) {
						sb.append(" &");
					}
				}
				if(i==0) {
					sb.append("\\\\\\hline\n");
				}
				else if(i!=matrix_h-1) {
					sb.append(" \\\\\n");
				}
				else if(i==matrix_h-1) {
					sb.append("\\\\\\hline\n");
				}
				txa.append(sb.toString());
			}
			txa.append("\t\\end{tabular}\n");
			txa.append("\t \\end{center} \n");
			txa.append("\t \\caption{"+caption.getText()+"} \n");
			txa.append("\\end{table}\n");

			show.add(txa,BorderLayout.CENTER);
			show.setSize(400, 300);
			//show.setDefaultCloseOperation(EXIT_ON_CLOSE);
			show.setVisible(true);
		}
	}
	class AddColumn implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(matrix_w>0) {
				dataSave();
				matrix_w++;
				subWindow.dispose();
				mainframe.dispose();
				refresh(matrix_h,matrix_w-1);	//いままでのmatrix
				tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
			}
		}
	}
	class DecColumn implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(matrix_w>1) {
				dataSave();
				matrix_w--;
				subWindow.dispose();
				mainframe.dispose();
				refresh(matrix_h,matrix_w+1);
				forcused_text_y=1;
				forcused_text_x=1;
				tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
			}
		}
	}
	class AddRow implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(matrix_h>0) {
				dataSave();
				matrix_h++;
				subWindow.dispose();
				mainframe.dispose();
				refresh(matrix_h-1,matrix_w);
				tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
			}
		}
	}
	class DecRow implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(matrix_h>1) {
				dataSave();
				matrix_h--;
				subWindow.dispose();
				mainframe.dispose();
				refresh(matrix_h+1,matrix_w);
				forcused_text_y=1;
				forcused_text_x=1;
				tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
			}
		}
	}
	class LoadCSVU8 implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			JFrame parent=new JFrame();
			File loadcsv = null;
			JFileChooser csv_get=new JFileChooser();
			FileNameExtensionFilter csv_filter = new FileNameExtensionFilter("*.csv","csv");
			csv_get.setFileFilter(csv_filter);
			if(csv_get.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {    // (3), (4)
				loadcsv = csv_get.getSelectedFile();
			}
			if(csv_get!=null) {
				try {
					FileInputStream input=new FileInputStream(loadcsv);
					InputStreamReader st=new InputStreamReader(input,"UTF-8");
					BufferedReader br=new BufferedReader(new FileReader(loadcsv));
					int H=0;
					int W=0;
					String[] tmp;
					String read=br.readLine();

					H++;
					int comma=0;
					if(read!=null) {
						comma=read.indexOf(",");
						if(comma<0) {
							tmp= new String[1];
							tmp[0]="";
						}
						else {
							tmp=read.split(",");
						}
						W=Math.max(W,tmp.length);
					}
					else if(read==null) {
						tmp= new String[1];
						tmp[0]="";
					}

					while(read!=null) {
						read=br.readLine();
						if(read!=null) {
							comma=read.indexOf(",");
							if(comma>=0) {
								tmp=read.split(",");
								W=Math.max(W,tmp.length);
							}
							H++;
						}
						else if(read==null) {
							break;
						}
					}
					matrix_h=Math.max(H,1);
					matrix_w=Math.max(W,1);
					data=new String[matrix_h][matrix_w];
					br=new BufferedReader(new FileReader(loadcsv));
					for(int i=0; i<H; i++) {
						read=br.readLine();
						if(read!=null) {
							comma=read.indexOf(",");
							if(comma>=0) {
								tmp=read.split(",");
								for(int j=0; j<W; j++) {
									if(j<tmp.length) {
										data[i][j]=tmp[j];
									}
									else {
										data[i][j]="";
									}
								}
							}
						}
						else {
							for(int j=0; j<W; j++) {
								data[i][j]="";
							}
						}
					}
					forcused_text_x=1;
					forcused_text_y=1;
					subWindow.dispose();
					mainframe.dispose();
					OpenCSV();	//いままでのmatrix
					tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
				} catch (IOException e1) {
					//
				}
			}
		}
	}
	class LoadCSVSJ implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			JFrame parent=new JFrame();
			File loadcsv = null;
			JFileChooser csv_get=new JFileChooser();
			FileNameExtensionFilter csv_filter = new FileNameExtensionFilter("*.csv","csv");
			csv_get.setFileFilter(csv_filter);
			if(csv_get.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				loadcsv = csv_get.getSelectedFile();
			}
			else {
				//
			}
			if(csv_get!=null) {
				try {
					FileInputStream input=new FileInputStream(loadcsv);
					InputStreamReader st=new InputStreamReader(input,"Shift_JIS");
					BufferedReader br=new BufferedReader(new FileReader(loadcsv));
					int H=0;
					int W=0;
					String[] tmp;
					String read=br.readLine();
					H++;
					int comma=0;
					if(read!=null) {
						comma=read.indexOf(",");
						if(comma<0) {
							tmp= new String[1];
							tmp[0]="";
						}
						else {
							tmp=read.split(",");
						}
						W=Math.max(W,tmp.length);
					}
					else if(read==null) {
						tmp= new String[1];
						tmp[0]="";
					}

					while(read!=null) {
						read=br.readLine();
						if(read!=null) {
							comma=read.indexOf(",");
							if(comma>=0) {
								tmp=read.split(",");
								W=Math.max(W,tmp.length);
							}
							H++;
						}
						else if(read==null) {
							break;
						}
					}
					matrix_h=Math.max(H,1);
					matrix_w=Math.max(W,1);
					data=new String[matrix_h][matrix_w];
					br=new BufferedReader(new FileReader(loadcsv));
					for(int i=0; i<H; i++) {
						read=br.readLine();
						if(read!=null) {
							comma=read.indexOf(",");
							if(comma>=0) {
								tmp=read.split(",");
								for(int j=0; j<W; j++) {
									if(j<tmp.length) {
										data[i][j]=tmp[j];
									}
									else {
										data[i][j]="";
									}
								}
							}
						}
						else {
							for(int j=0; j<W; j++) {
								data[i][j]="";
							}
						}
					}
					forcused_text_x=1;
					forcused_text_y=1;
					subWindow.dispose();
					mainframe.dispose();
					OpenCSV();	//いままでのmatrix
					tx[forcused_text_y][forcused_text_x].requestFocusInWindow();
				} catch (IOException e1) {
					//
				}
			}
		}
	}
}