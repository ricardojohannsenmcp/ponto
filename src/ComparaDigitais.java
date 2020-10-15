import com.nitgen.SDK.BSP.NBioBSPJNI;

public class ComparaDigitais {

	public static void main(String[] args) {

		Boolean bResult = new Boolean(false);
		NBioBSPJNI bsp =  new NBioBSPJNI();
		NBioBSPJNI.DEVICE_ENUM_INFO deviceEnumInfo = bsp.new DEVICE_ENUM_INFO();
		NBioBSPJNI.FIR_HANDLE hCapturedFIR = bsp.new FIR_HANDLE();
		NBioBSPJNI.FIR_TEXTENCODE m_textFIR = bsp.new FIR_TEXTENCODE();

		NBioBSPJNI.FIR_PAYLOAD payload = bsp.new FIR_PAYLOAD();
		m_textFIR.TextFIR= "sua digital";
		//abre o dispositivo
		bsp.OpenDevice(deviceEnumInfo.DeviceInfo[0].NameID,deviceEnumInfo.DeviceInfo[0].Instance);
		bsp.Capture(hCapturedFIR);
		NBioBSPJNI.INPUT_FIR firDatabase =  bsp.new  INPUT_FIR();
		firDatabase.SetTextFIR(m_textFIR);
		NBioBSPJNI.INPUT_FIR firLeitura =  bsp.new INPUT_FIR();
		NBioBSPJNI.FIR_TEXTENCODE m_textFIRLeitura = bsp.new FIR_TEXTENCODE();
		bsp.GetTextFIRFromHandle(hCapturedFIR, m_textFIRLeitura);
		firLeitura.SetTextFIR(m_textFIRLeitura);
		bsp.VerifyMatch(firLeitura,firDatabase,bResult, payload);
		if (bsp.IsErrorOccured() == false) {
			if (bResult)
				System.out.println("OK");
			else
				System.out.println("Verify Failed");
		}
	}

}
