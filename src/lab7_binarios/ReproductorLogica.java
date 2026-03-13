/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_binarios;


/**
 *
 * @author emyca
 */

import java.io.*;
import javazoom.jl.player.Player;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.Image;

public class ReproductorLogica {
    private RandomAccessFile listaReproduccion;
    
    // Datos de la canción seleccionada
    private String artistaCancionSelec;
    private String rutaCancionSelec;
    private ImageIcon imagenCancionSelec;
    private String nombreCancionSelec;
    private String generoCancionSelec;
    private String duracionCancionSelec;

    // Control de Audio (JLayer)
    private Player player;
    private Thread hiloReproduccion;
    private boolean estaReproduciendo = false;
    private boolean estaPausado = false;
    
    // Control de Pausa (Bytes)
    private FileInputStream fis;
    private long totalBytes = 0;
    private long bytesPausados = 0;

    public ReproductorLogica() {
        try {
            // Asegúrate de que la ruta coincida con tu estructura de NetBeans
            File archivoBin = new File("src/ListaDeReproduccion.emp");
            listaReproduccion = new RandomAccessFile(archivoBin, "rw");
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null,"Error: No se pudo crear/abrir el archivo binario.");
        }
    }

    // --- GESTIÓN DE ARCHIVO BINARIO ---

    public void Add(String rutaCancion, String nombre, String artista, String duracion, String rutaImagen, String genero) throws IOException {
        listaReproduccion.seek(listaReproduccion.length());
        listaReproduccion.writeUTF(nombre);
        listaReproduccion.writeUTF(rutaCancion);
        listaReproduccion.writeUTF(artista);
        listaReproduccion.writeUTF(duracion);
        listaReproduccion.writeUTF(rutaImagen);
        listaReproduccion.writeUTF(genero);
        listaReproduccion.writeBoolean(true); // Activa por defecto
    }

    public void cargarCancionesDefault() {
        try {
            if (listaReproduccion.length() == 0) {
                // Rutas de ejemplo (ajusta a tus archivos reales)
                Add("src/CancionesDefault/cancion1.mp3", "Cheekbones", "Arrows in Action", "2:27", "src/CancionesDefault/cheekbones.jpg", "Pop-Rock");
                Add("src/Cancionesdefault/cancion2.mp3", "Dime Como Quieres", "Christian Nodal", "2:49", "src/CancionesDefault/dime.jpg", "Regional Mexicano");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar defaults: " + e.getMessage());
        }
    }

    public boolean Select(String nombre) throws IOException {
        listaReproduccion.seek(0);
        while (listaReproduccion.getFilePointer() < listaReproduccion.length()) {
            String name = listaReproduccion.readUTF();
            String ruta = listaReproduccion.readUTF();
            String artista = listaReproduccion.readUTF();
            String duracion = listaReproduccion.readUTF();
            String img = listaReproduccion.readUTF();
            String genero = listaReproduccion.readUTF();
            boolean activa = listaReproduccion.readBoolean();

            if (name.equals(nombre) && activa) {
                this.nombreCancionSelec = name;
                this.rutaCancionSelec = ruta;
                this.artistaCancionSelec = artista;
                this.duracionCancionSelec = duracion;
                this.generoCancionSelec = genero;
                setImagen(img);
                
                // Si cambiamos de canción, reseteamos el estado de pausa
                Stop(); 
                return true;
            }
        }
        return false;
    }

    public int Remove(String nombre) throws IOException {
        listaReproduccion.seek(0);
        while (listaReproduccion.getFilePointer() < listaReproduccion.length()) {
            String name = listaReproduccion.readUTF();
            listaReproduccion.readUTF(); // ruta
            listaReproduccion.readUTF(); // artista
            listaReproduccion.readUTF(); // duracion
            listaReproduccion.readUTF(); // imagen
            listaReproduccion.readUTF(); // genero
            
            long posicion = listaReproduccion.getFilePointer();
            boolean activo = listaReproduccion.readBoolean();

            if (name.equals(nombre)) {
                if (!activo) 
                    return 2;
                
                if (name.equals(nombreCancionSelec)) 
                    Stop();
                
                listaReproduccion.seek(posicion);
                listaReproduccion.writeBoolean(false);
                return 1;
            }
        }
        return 3; // No encontrado
    }

    // --- MOTOR DE REPRODUCCIÓN (JLAYER) ---

    public void Play() {
        if (rutaCancionSelec == null || rutaCancionSelec.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una canción.");
            return;
        }

        if (estaReproduciendo) return;

        hiloReproduccion = new Thread(() -> {
            try {
                File file = new File(rutaCancionSelec);
                totalBytes = file.length();
                fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);

                if (estaPausado) {
                    // Saltamos lo que ya escuchamos
                    fis.skip(totalBytes - bytesPausados);
                }

                player = new Player(bis);
                estaReproduciendo = true;
                estaPausado = false;
                player.play();

                if (player.isComplete()) {
                    estaReproduciendo = false;
                    bytesPausados = 0;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,"Error en reproducción: " + e.getMessage());
            } finally {
                estaReproduciendo = false;
            }
        });
        hiloReproduccion.start();
    }

    public void Pause() {
        if (estaReproduciendo && player != null) {
            try {
                bytesPausados = fis.available(); // Guardamos el remanente
                player.close();
                estaReproduciendo = false;
                estaPausado = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Stop() {
        if (player != null) {
            player.close();
        }
        estaReproduciendo = false;
        estaPausado = false;
        bytesPausados = 0;
    }

    // --- GETTERS ---

    public void setImagen(String ruta) {
        ImageIcon icon = new ImageIcon(ruta);
        Image img = icon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
        this.imagenCancionSelec = new ImageIcon(img);
    }

    public ImageIcon getImagen() { 
        return imagenCancionSelec; 
    }
    
    public String getArtista() { 
        return artistaCancionSelec; 
    }
    
    public String getGenero() { 
        return generoCancionSelec; 
    }
    
    public String getNombre() { 
        return nombreCancionSelec; 
    }
    
    public String getDuracion() { 
        return duracionCancionSelec; 
    }

    public void cerrar() throws IOException {
        Stop();
        if (listaReproduccion != null) 
            listaReproduccion.close();
    }
}