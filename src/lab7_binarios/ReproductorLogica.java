/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
    package lab7_binarios;

    import java.io.*;
    import javazoom.jl.player.Player;
    import javax.swing.ImageIcon;
    import javax.swing.JOptionPane;
    import java.awt.Image;

    public class ReproductorLogica {
        private RandomAccessFile listaReproduccion;

        private String artistaCancionSelec;
        private String rutaCancionSelec; 
        private ImageIcon imagenCancionSelec;
        private String nombreCancionSelec;
        private String generoCancionSelec;
        private String duracionCancionSelec;

        private Player player;
        private Thread hiloReproduccion;
        private boolean estaReproduciendo = false;
        private boolean estaPausado = false;

        private FileInputStream fis;
        private long totalBytes = 0;
        private long bytesPausados = 0;

        public ReproductorLogica() {
            try {
                File archivoBin = new File("src/ListaDeReproduccion.emp");
                listaReproduccion = new RandomAccessFile(archivoBin, "rw");
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null,"Error: No se pudo abrir el archivo binario.");
            }
        }

        private byte[] obtenerBytes(String ruta) throws IOException {
            File file = new File(ruta);
            byte[] buffer = new byte[(int) file.length()];
            try (FileInputStream in = new FileInputStream(file)) {
                in.read(buffer);
            }
            return buffer;
        }

        public void Add(String rutaCancion, String nombre, String artista, String duracion, String rutaImagen, String genero) throws IOException {
            listaReproduccion.seek(listaReproduccion.length());
            listaReproduccion.writeUTF(nombre);
            listaReproduccion.writeUTF(artista);
            listaReproduccion.writeUTF(duracion);
            listaReproduccion.writeUTF(genero);

            byte[] audio = obtenerBytes(rutaCancion);
            listaReproduccion.writeInt(audio.length);
            listaReproduccion.write(audio);

            byte[] imagen = obtenerBytes(rutaImagen);
            listaReproduccion.writeInt(imagen.length);
            listaReproduccion.write(imagen);

            listaReproduccion.writeBoolean(true);
        }

        public void cargarCancionesDefault() {
            try {
                if (listaReproduccion.length() == 0) {
                    Add("src/CancionesDefault/cancion1.mp3", "Cheekbones", "Arrows in Action", "2:27", "src/CancionesDefault/cheekbones.jpg", "Pop-Rock");
                    Add("src/CancionesDefault/cancion2.mp3", "Figure You Out", "VOILA", "3:19", "src/CancionesDefault/figure.jpg", "Indie");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error al cargar defaults: " + e.getMessage());
            }
        }

        public boolean Select(String nombre) throws IOException {
            listaReproduccion.seek(0);
            while (listaReproduccion.getFilePointer() < listaReproduccion.length()) {
                String name = listaReproduccion.readUTF();
                String artista = listaReproduccion.readUTF();
                String duracion = listaReproduccion.readUTF();
                String genero = listaReproduccion.readUTF();

                int audioLen = listaReproduccion.readInt();
                byte[] audioBytes = new byte[audioLen];
                listaReproduccion.read(audioBytes);

                int imgLen = listaReproduccion.readInt();
                byte[] imgBytes = new byte[imgLen];
                listaReproduccion.read(imgBytes);

                boolean activa = listaReproduccion.readBoolean();

                if (name.equals(nombre) && activa) {
                    this.nombreCancionSelec = name;
                    this.artistaCancionSelec = artista;
                    this.duracionCancionSelec = duracion;
                    this.generoCancionSelec = genero;

                    File tempMp3 = File.createTempFile("playing", ".mp3");
                    tempMp3.deleteOnExit();
                    try (FileOutputStream fos = new FileOutputStream(tempMp3)) {
                        fos.write(audioBytes);
                    }
                    this.rutaCancionSelec = tempMp3.getAbsolutePath();

                    ImageIcon icon = new ImageIcon(imgBytes);
                    Image img = icon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                    this.imagenCancionSelec = new ImageIcon(img);

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
                listaReproduccion.readUTF();  
                listaReproduccion.readUTF();  
                listaReproduccion.readUTF();  

                int aLen = listaReproduccion.readInt();
                listaReproduccion.skipBytes(aLen);   

                int iLen = listaReproduccion.readInt();
                listaReproduccion.skipBytes(iLen);   

                long posicion = listaReproduccion.getFilePointer();
                boolean activo = listaReproduccion.readBoolean();

                if (name.equals(nombre)) {
                    if (!activo) return 2;
                    if (name.equals(nombreCancionSelec)) Stop();

                    listaReproduccion.seek(posicion);
                    listaReproduccion.writeBoolean(false);
                    return 1;
                }
            }
            return 3;
        }

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
                    bytesPausados = fis.available();
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
    }