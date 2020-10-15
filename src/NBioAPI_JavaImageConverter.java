import com.nitgen.SDK.BSP.NBioBSPJNI;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class NBioAPI_JavaImageConverter extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int FRAME_WIDTH = 430;
	private static final int FRAME_HEIGHT = 500;
	
	private static final int START_Y_POS = 50;
	
	private static final int CONTROL_START_POS_X = 20;
	private static final int CONTROL_START_POS_Y = 30;	
	
	private static final int BUTTON_SIZE_WIDTH = 250;
	private static final int BUTTON_SIZE_HEIGHT = 30;
	
	private static final int CONTROL_INCREASE_POS_Y = BUTTON_SIZE_HEIGHT + 10;
	
	public NBioAPI_JavaImageConverter() {
		this.setLayout(null);
		
		// All buttons disabled
		m_btnCapture.setEnabled(false);
		m_btnRawToBmp.setEnabled(false);
		m_btnBmpToRaw.setEnabled(false);
		m_btnRawToFir.setEnabled(false);
		
		// BSP Initialize
		Init();
		
		// label for result
		m_labelResult.setSize(FRAME_WIDTH, 30);
		m_labelResult.setLocation(5, FRAME_HEIGHT - 70);
		this.add(m_labelResult);
		
		// Capture Button
		m_btnCapture.setSize(120, 25);
		m_btnCapture.setLocation(100, START_Y_POS - 40);
		
		this.add(m_btnCapture);
		
		// RAW <-> BMP
		m_panRawToBmp.setLayout(null);
		m_panRawToBmp.setBorder(BorderFactory.createTitledBorder("RAW <-> BMP"));
		m_panRawToBmp.setSize(400, 200);
		m_panRawToBmp.setLocation(5, START_Y_POS);
		this.add(m_panRawToBmp);
		
		int nXPos = CONTROL_START_POS_X;
		int nYPos = CONTROL_START_POS_Y;
		
		AddComponent(m_panRawToBmp, m_btnRawToBmp, nXPos, nYPos, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
		nYPos += CONTROL_INCREASE_POS_Y;
		AddComponent(m_panRawToBmp, m_btnBmpToRaw, nXPos, nYPos, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
		nYPos += CONTROL_INCREASE_POS_Y;
		AddComponent(m_panRawToBmp, m_btnRawToFir, nXPos, nYPos, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
		
		SetEventHandler();
	}
	
	private void AddComponent(JPanel pan, Component obj, int x, int y, int width, int height) {
		obj.setSize(width, height);
		obj.setLocation(x, y);
		pan.add(obj);
	}
	
	private void SetEventHandler() {
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (m_hCaptured != null)
					m_hCaptured.dispose();
				if (m_hAudit != null)
					m_hAudit.dispose();
				
				m_bsp.dispose();
				dispose();				
				System.exit(0);
			}
		});
		
		m_btnCapture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OnCapture();
			}
		});
		
		m_btnRawToBmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OnRawToBmp();
			}
		});
		
		m_btnBmpToRaw.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OnBmpToRaw();
			}
		});
		
		m_btnRawToFir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OnRawToFirVerify();
			}
		});
	}
	
	private void OnCapture() {
		if (m_hCaptured != null)
		{
			m_hCaptured.dispose();
			m_hCaptured = null;
		}
		
		if (m_hAudit != null)
		{
			m_hAudit.dispose();
			m_hAudit = null;
		}
		
		m_hCaptured = m_bsp.new FIR_HANDLE();
		m_hAudit = m_bsp.new FIR_HANDLE();
		
		m_bsp.Capture(NBioBSPJNI.FIR_PURPOSE.VERIFY, m_hCaptured, -1, m_hAudit, null);
		if (CheckError("Capture()"))
			return;
		
		// Get Raw Image
		m_RawBuf = null;
		m_RawWidth = 0;
		m_RawHeight = 0;
		
		NBioBSPJNI.INPUT_FIR inputFIR = m_bsp.new INPUT_FIR();
		inputFIR.SetFIRHandle(m_hAudit);
		
		NBioBSPJNI.Export.AUDIT exportAudit = m_export.new AUDIT();
		
		m_export.ExportAudit(inputFIR, exportAudit);
		if (CheckError("ExportAudit("))
			return;
		
		m_RawWidth = exportAudit.ImageWidth;
		m_RawHeight = exportAudit.ImageHeight;
		m_RawBuf = exportAudit.FingerData[0].Template[0].Data;
		
		WriteFile("RawImage_" + m_RawWidth + "_" + m_RawHeight + ".raw", m_RawBuf);
		
		m_btnRawToBmp.setEnabled(true);
		m_labelResult.setText("Capture success!");
	}
	
	private void OnRawToBmp() {
		m_BmpBuf = null;
		
		NBioBSPJNI.Export.TEMPLATE_DATA bmpData = m_export.new TEMPLATE_DATA();
		m_export.ConvertRawToBmp(m_RawBuf, 248, 292, bmpData);
		
		if (CheckError("ConvertRawToBmp("))
			return;
		
		m_BmpBuf = bmpData.Data;
		
		WriteFile("BmpImageFromRaw.bmp", m_BmpBuf);
		m_labelResult.setText("ConvertRawToBmp success!");
		
		m_btnBmpToRaw.setEnabled(true);
	}
	
	private void OnBmpToRaw() {
		m_RawBuf = null;
		m_RawWidth = 0;
		m_RawHeight = 0;
		
		NBioBSPJNI.Export.AUDIT rawData = m_export.new AUDIT();
		m_export.ConvertBmpToRaw(m_BmpBuf, m_BmpBuf.length, rawData);
		if (CheckError("ConvertBmpToRaw("))
			return;
		
		m_RawWidth = rawData.ImageWidth;
		m_RawHeight = rawData.ImageHeight;
		m_RawBuf = rawData.FingerData[0].Template[0].Data;
		
		WriteFile("RawImageFromBmp.raw", m_RawBuf);
		m_labelResult.setText("ConvertBmpToRaw success!");
		
		m_btnRawToFir.setEnabled(true);
	}
	
	private void OnRawToFirVerify() {
		
		// create export audit data
		NBioBSPJNI.Export.AUDIT auditData = m_export.new AUDIT();
		
		auditData.FingerNum = 1;
		auditData.SamplesPerFinger = 1;
		auditData.ImageWidth = m_RawWidth;
		auditData.ImageHeight = m_RawHeight;
		
		auditData.FingerData = new NBioBSPJNI.Export.FINGER_DATA[1];
		auditData.FingerData[0] = m_export.new FINGER_DATA();
		auditData.FingerData[0].FingerID = NBioBSPJNI.FINGER_ID.UNKNOWN;
		
		auditData.FingerData[0].Template = new NBioBSPJNI.Export.TEMPLATE_DATA[1];
		auditData.FingerData[0].Template[0] = m_export.new TEMPLATE_DATA();
		auditData.FingerData[0].Template[0].Data = m_RawBuf;
		
		// Get captured fir from audit data
		NBioBSPJNI.FIR_HANDLE hCapturedFIR = m_bsp.new FIR_HANDLE();
		m_export.ImportAudit(auditData, hCapturedFIR);
		
		if (CheckError("ImportAudit()"))
			return;
		
		NBioBSPJNI.FIR_HANDLE hProcessFIR = m_bsp.new FIR_HANDLE();
		
		NBioBSPJNI.INPUT_FIR inputFIR = m_bsp.new INPUT_FIR();
		inputFIR.SetFIRHandle(hCapturedFIR);
		
		m_bsp.Process(inputFIR, hProcessFIR);
		if (CheckError("Process()"))
			return;
		
		// Verify
		Boolean bResult = new Boolean(false);
		inputFIR.SetFIRHandle(hProcessFIR);
		m_bsp.Verify(inputFIR, bResult, null);
		if (CheckError("Verify()"))
			return;
		
		hCapturedFIR.dispose();
		hProcessFIR.dispose();
		
		m_labelResult.setText("Verify Result : " + bResult);
	}
	
	public Boolean CheckError(String strCaller)
    {
        if (m_bsp.IsErrorOccured())  {
            m_labelResult.setText(strCaller + " NBioBSP Error Occured [" + m_bsp.GetErrorCode() + "]");
            return true;
        }

        return false;
    }
	
	private void Init() {
		m_bsp = new NBioBSPJNI();
		if (CheckError("Init()"))
			return;
		
		m_export = m_bsp.new Export();
		
		m_bsp.OpenDevice();
		if (CheckError("OpenDevice()"))
			return;
		
		setTitle("Image converter sample");
		m_labelResult.setText("BSP Initialize success!");
		
		m_btnCapture.setEnabled(true);
	}
	
	private Boolean WriteFile(String fileName, byte[] data)
    {
        java.io.File newFile = new java.io.File(fileName);
        java.io.DataOutputStream out;

        try  {
            out = new java.io.DataOutputStream(new java.io.FileOutputStream(newFile, false));
        }
        catch (java.io.FileNotFoundException ex)  {
        	m_labelResult.setText("File Creat failed!!");
            return false;
        }

        try  {
            out.write(data);
            out.close();
        }
        catch (java.io.IOException e)  {
            m_labelResult.setText("File Write failed!!");
            return false;
        }

        return true;
    }
	

	public static void main(String [] args) {
		NBioAPI_JavaImageConverter imgConv = new NBioAPI_JavaImageConverter();
		imgConv.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		imgConv.setVisible(true);
	}
	
	private NBioBSPJNI m_bsp;
	private NBioBSPJNI.Export m_export;
	private NBioBSPJNI.FIR_HANDLE m_hCaptured = null;
	private NBioBSPJNI.FIR_HANDLE m_hAudit = null;
	
	private byte[] m_RawBuf = null;
	private int m_RawWidth = 0;
	private int m_RawHeight = 0;
	private byte[] m_BmpBuf = null;
	
	// capture button
	private JButton m_btnCapture = new JButton("Capture");
	
	// BMP
	private JPanel m_panRawToBmp = new JPanel();
	private JButton m_btnRawToBmp = new JButton("RAW TO BMP");
	private JButton m_btnBmpToRaw = new JButton("BMP TO RAW");
	private JButton m_btnRawToFir = new JButton("RAW TO FIR & VERIFY");
	
	
	private JLabel m_labelResult = new JLabel();
}
