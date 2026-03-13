package lab7_binarios;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import javax.swing.filechooser.*;
import java.io.RandomAccessFile;
import java.io.File;

public class ReproductorMusica extends JFrame {

    private JPanel panelDerechoContenedor;
    private CardLayout cardLayout;
    private JList<String> listaCanciones;
    private DefaultListModel<String> modeloLista;
    private ReproductorLogica logica;

    private JLabel lblImagen, lblNombre, lblArtista, lblDuracion, lblGenero;

    private JTextField txtNombre, txtArtista, txtDuracion, txtGenero, txtRutaMusica, txtRutaImagen;
    private String pathCancion = "", pathImagen = "";

    public ReproductorMusica() {
        logica = new ReproductorLogica();
        logica.cargarCancionesDefault();

        setTitle("Reproductor de Musica MP3");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel sidebar = new JPanel(null);
        sidebar.setBounds(0, 0, 280, 600);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JLabel lblPlaylist = new JLabel("Mi Playlist");
        lblPlaylist.setFont(new Font("Arial", Font.BOLD, 16));
        lblPlaylist.setBounds(20, 20, 100, 25);
        sidebar.add(lblPlaylist);

        modeloLista = new DefaultListModel<>();
        listaCanciones = new JList<>(modeloLista);
        JScrollPane scroll = new JScrollPane(listaCanciones);
        scroll.setBounds(20, 55, 240, 300);
        sidebar.add(scroll);

        actualizarListaVisual();

        JButton btnSelect = new JButton("Select");
        btnSelect.setBounds(20, 370, 240, 35);
        btnSelect.addActionListener(e -> {
            String seleccion = listaCanciones.getSelectedValue();
            if (seleccion != null) {
                try {
                    if (logica.Select(seleccion)) {
                        lblNombre.setText(logica.getNombre());
                        lblArtista.setText("Artista: " + logica.getArtista());
                        lblDuracion.setText("Duración: " + logica.getDuracion());
                        lblGenero.setText("Género: " + logica.getGenero());
                        lblImagen.setIcon(logica.getImagen());
                        cardLayout.show(panelDerechoContenedor, "DETALLES");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una canción de la lista.");
            }
        });
        sidebar.add(btnSelect);

        JButton btnAdd = new JButton("Add New");
        btnAdd.setBounds(20, 415, 240, 35);
        btnAdd.addActionListener(e -> cardLayout.show(panelDerechoContenedor, "FORM_ADD"));
        sidebar.add(btnAdd);

        JButton btnRemove = new JButton("Remove");
        btnRemove.setBounds(20, 460, 240, 35);
        btnRemove.addActionListener(e -> cardLayout.show(panelDerechoContenedor, "FORM_REMOVE"));
        sidebar.add(btnRemove);

        add(sidebar);

        cardLayout = new CardLayout();
        panelDerechoContenedor = new JPanel(cardLayout);
        panelDerechoContenedor.setBounds(280, 0, 620, 600);

        panelDerechoContenedor.add(crearPanelDetalles(), "DETALLES");
        panelDerechoContenedor.add(crearPanelFormularioAdd(), "FORM_ADD");
        panelDerechoContenedor.add(crearPanelFormularioRemove(), "FORM_REMOVE");

        add(panelDerechoContenedor);
        setVisible(true);
    }

    private JPanel crearPanelDetalles() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        lblImagen = new JLabel("Sin Imagen", SwingConstants.CENTER);
        lblImagen.setBounds(40, 60, 250, 250);
        lblImagen.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(lblImagen);

        int xInfo = 310;
        lblNombre = new JLabel("Seleccione canción");
        lblNombre.setFont(new Font("Arial", Font.BOLD, 24));
        lblNombre.setBounds(xInfo, 60, 280, 35);
        panel.add(lblNombre);

        lblArtista = new JLabel("Artista: ---");
        lblArtista.setFont(new Font("Arial", Font.PLAIN, 18));
        lblArtista.setForeground(Color.DARK_GRAY);
        lblArtista.setBounds(xInfo, 100, 280, 30);
        panel.add(lblArtista);

        lblDuracion = new JLabel("Duración: --:--");
        lblDuracion.setFont(new Font("Arial", Font.PLAIN, 16));
        lblDuracion.setBounds(xInfo, 140, 280, 25);
        panel.add(lblDuracion);

        lblGenero = new JLabel("Género: ---");
        lblGenero.setFont(new Font("Arial", Font.PLAIN, 16));
        lblGenero.setBounds(xInfo, 170, 280, 25);
        panel.add(lblGenero);

        JPanel panelReproduccion = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        panelReproduccion.setBounds(0, 450, 620, 70);
        panelReproduccion.setOpaque(false);

        JButton btnPlay = new JButton("PLAY ▶");
        JButton btnPause = new JButton("PAUSE ⏸");
        JButton btnStop = new JButton("STOP ⏹");

        btnPlay.addActionListener(e -> logica.Play());
        btnPause.addActionListener(e -> logica.Pause());
        btnStop.addActionListener(e -> logica.Stop());

        panelReproduccion.add(btnPlay);
        panelReproduccion.add(btnPause);
        panelReproduccion.add(btnStop);
        panel.add(panelReproduccion);

        return panel;
    }

    private JPanel crearPanelFormularioAdd() {
        JPanel panel = new JPanel(null);
        JLabel titulo = new JLabel("Registrar Nueva Canción");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setBounds(40, 10, 250, 30);
        panel.add(titulo);

        txtNombre = new JTextField();
        txtArtista = new JTextField();
        txtDuracion = new JTextField();
        txtGenero = new JTextField();
        txtRutaMusica = new JTextField();
        txtRutaImagen = new JTextField();

        String[] etiquetas = {"Nombre:", "Artista:", "Duración (min/seg):", "Género:", "Archivo MP3:", "Imagen:"};
        JTextField[] fields = {txtNombre, txtArtista, txtDuracion, txtGenero, txtRutaMusica, txtRutaImagen};

        int y = 50;
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setBounds(40, y, 150, 20);
            panel.add(lbl);

            fields[i].setBounds(40, y + 20, 250, 30);
            if (i >= 4) fields[i].setEditable(false);
            panel.add(fields[i]);

            if (i == 4 || i == 5) {
                JButton btnFile = new JButton("...");
                btnFile.setBounds(300, y + 20, 45, 30);
                final int index = i;
                btnFile.addActionListener(e -> {
                    JFileChooser chooser = new JFileChooser();
                    if (index == 4) {
                        chooser.setFileFilter(new FileNameExtensionFilter("Archivos MP3", "mp3"));
                    } else {
                        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));
                    }

                    if (chooser.showOpenDialog(ReproductorMusica.this) == JFileChooser.APPROVE_OPTION) {
                        String path = chooser.getSelectedFile().getAbsolutePath();
                        fields[index].setText(path);
                        if (index == 4) pathCancion = path; else pathImagen = path;
                    }
                });
                panel.add(btnFile);
            }
            y += 60;
        }

        JButton btnGuardar = new JButton("Guardar en Playlist");
        btnGuardar.setBounds(40, y, 350, 40);
        btnGuardar.setBackground(new Color(46, 204, 113));
        btnGuardar.setForeground(Color.WHITE);

        btnGuardar.addActionListener(e -> {
            try {
                logica.Add(pathCancion, txtNombre.getText(), txtArtista.getText(), txtDuracion.getText(), pathImagen, txtGenero.getText());
                actualizarListaVisual();
                JOptionPane.showMessageDialog(this, "Canción guardada");
                cardLayout.show(panelDerechoContenedor, "DETALLES");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar");
            }
        });

        panel.add(btnGuardar);
        return panel;
    }

    private JPanel crearPanelFormularioRemove() {
        JPanel panel = new JPanel(null);
        JLabel titulo = new JLabel("Eliminar de la Playlist");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setBounds(40, 20, 250, 30);
        panel.add(titulo);

        JTextField txtRemove = new JTextField();
        txtRemove.setBounds(40, 110, 350, 35);
        panel.add(txtRemove);

        JButton btnConfirmar = new JButton("Confirmar Eliminación");
        btnConfirmar.setBounds(40, 160, 350, 40);
        btnConfirmar.setBackground(new Color(231, 76, 60));
        btnConfirmar.setForeground(Color.WHITE);

        btnConfirmar.addActionListener(e -> {
            try {
                int res = logica.Remove(txtRemove.getText());
                if (res == 1) {
                    JOptionPane.showMessageDialog(this, "Eliminada.");
                    actualizarListaVisual();
                } else {
                    JOptionPane.showMessageDialog(this, "No encontrada");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(btnConfirmar);
        return panel;
    }

    private void actualizarListaVisual() {
        modeloLista.clear();
        try {
            File archivo = new File("src/ListaDeReproduccion.emp");
            if (!archivo.exists()) return;
            RandomAccessFile raf = new RandomAccessFile(archivo, "r");
            while (raf.getFilePointer() < raf.length()) {
                String nombre = raf.readUTF();
                raf.readUTF();
                raf.readUTF();
                raf.readUTF();
                raf.readUTF();
                raf.readUTF();
                if (raf.readBoolean()) {
                    modeloLista.addElement(nombre);
                }
            }
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}