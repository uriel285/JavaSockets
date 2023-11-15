package chats;

import java.io.*;
import java.net.*;

class Conexiones implements Runnable {

	ServidorDeChat servidor = null;
	private Socket socketDeComunicacion = null;
	private OutputStreamWriter corrienteDeSalida = null;
	private BufferedReader corrienteDeEntrada = null;

	public Conexiones(ServidorDeChat servidor, Socket s) {
		this.servidor = servidor;
		this.socketDeComunicacion = s;
	}

	public void sendMessage(String mensaje) {
		try {
			corrienteDeSalida.write(mensaje);
			corrienteDeSalida.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		OutputStream socketDeSalida = null;
		InputStream socketDeEntrada = null;
		String laFrase = servidor.getFraseDeAcceso();
		String cliente = null;

		try {
			socketDeSalida = socketDeComunicacion.getOutputStream();
			corrienteDeSalida = new OutputStreamWriter(socketDeSalida);
			socketDeEntrada = socketDeComunicacion.getInputStream();
			corrienteDeEntrada = new BufferedReader(new InputStreamReader(
					socketDeEntrada));

			InetAddress direccion = socketDeComunicacion.getInetAddress();
			String nombreDelServidor = direccion.getHostName();

			String bienvenida = "Se realiz� la conexi�n al servidor: "
					+ nombreDelServidor + "\nTodo el mundo salude por favor ";
			String cli = servidor.getNombreDelCliente(nombreDelServidor);
			cliente = cli == null? "dice el servidor. ": cli;
			if (cliente != null)
				bienvenida += " a " + cliente;
			bienvenida += "!\n";
			servidor.enviarATodosLosClientes(bienvenida);
			System.out.println("Conexi�n realizada: " + cliente + "@"
					+ nombreDelServidor);
			sendMessage("Bienvenido " + cliente + "La frase clave es "
					+ laFrase + "\n");
			String entrada = null;

			while ((entrada = corrienteDeEntrada.readLine()) != null) {
				if (entrada.indexOf(laFrase) != -1) {
					servidor.reproducirSonido();
					sendMessage("Felicitaciones " + cliente
							+ " Acaba de enviar la frase clave!\n");
					System.out.println(cliente + "Envi� la frase clave!");
				} else {
					servidor.enviarATodosLosClientes(entrada + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			try {
				if (corrienteDeEntrada != null)
					corrienteDeEntrada.close();
				if (corrienteDeSalida != null)
					corrienteDeSalida.close();
				socketDeComunicacion.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			servidor.cerrarConexiones(this);
		}
	}
}
