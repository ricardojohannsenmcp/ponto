import com.nitgen.SDK.BSP.NBioBSPJNI;

public class CapturaDigital {

	public static void main(String[] args) {
	
		//cria a instancia do objeto do leitor
		NBioBSPJNI bsp =  new NBioBSPJNI();
		//lê as informações do dispositivo
		NBioBSPJNI.DEVICE_ENUM_INFO deviceEnumInfo = bsp.new DEVICE_ENUM_INFO();
		//lê a porta do dispositivo
		//int enumerateDevice = bsp.EnumerateDevice(deviceEnumInfo);
		bsp.OpenDevice(deviceEnumInfo.DeviceInfo[0].NameID,deviceEnumInfo.DeviceInfo[0].Instance);
		NBioBSPJNI.FIR_HANDLE hSavedFIR = bsp.new FIR_HANDLE();
		bsp.Capture(hSavedFIR);
		NBioBSPJNI.FIR_TEXTENCODE textSavedFIR = null;
		//se houve exito na captura
		if (bsp.IsErrorOccured() == false) {
			textSavedFIR = bsp.new FIR_TEXTENCODE();
			 bsp.GetTextFIRFromHandle(hSavedFIR, textSavedFIR);
			 System.out.println("captura: "+textSavedFIR.TextFIR);
			
			}
	}

}
