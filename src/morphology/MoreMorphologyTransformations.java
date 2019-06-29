package morphology;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import util.FileInfo;
import util.Resizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MoreMorphologyTransformations {

    private static final String[] MORPH_OP = {"Opening", "Closing", "Gradient", "Top Hat", "Black Hat"};
    private static final int[] MORPH_OP_TYPE = {
            Imgproc.MORPH_OPEN,
            Imgproc.MORPH_CLOSE,
            Imgproc.MORPH_GRADIENT,
            Imgproc.MORPH_TOPHAT,
            Imgproc.MORPH_BLACKHAT
    };
    private static final String[] ELEMENT_TYPE = {"Ellipse", "Rectangle", "Cross"};
    private static final int MAX_KERNEL_SIZE = 21;
    private Mat matImgSrc;
    private Mat matImgDst = new Mat();
    private int morphOpType = Imgproc.MORPH_OPEN;
    private int elementType = Imgproc.CV_SHAPE_RECT;
    private int kernelSize = 0;
    private JFrame frame;
    private JLabel imgLabel;
    private FileInfo fileInfo;

    private MoreMorphologyTransformations() {
        fileInfo = new FileInfo();
        String imagePath = fileInfo.getPath();
        matImgSrc = Imgcodecs.imread(imagePath);
        if (matImgSrc.empty()) {
            System.err.println("Empty image: " + imagePath);
            System.exit(0);
        } else if (matImgSrc.height() > 600) matImgSrc = Imgcodecs.imread(Resizer.resize_then_save(
                imagePath,
                matImgSrc.width() * 600 / matImgSrc.height(),
                600
        ));

        // Create and set up the window.
        frame = new JFrame("Morphology Transformations demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set up the content pane.
        Image img = HighGui.toBufferedImage(matImgSrc);
        addComponentsToPane(frame.getContentPane(), img);
        // Use the content pane's default BorderLayout. No need for
        // setLayout(new BorderLayout());
        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private void addComponentsToPane(Container pane, Image img) {
        if (!(pane.getLayout() instanceof BorderLayout)) {
            pane.add(new JLabel("Container doesn't use BorderLayout!"));
            return;
        }

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.PAGE_AXIS));
        JPanel ctrlPanel = new JPanel();
        ctrlPanel.setBackground(Color.LIGHT_GRAY);

        JButton button = new JButton("Test another image");
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new MoreMorphologyTransformations();
            }
        });
        ctrlPanel.add(button, BorderLayout.LINE_START);

        JComboBox<String> elementTypeBox = new JComboBox<>(ELEMENT_TYPE);
        elementTypeBox.addActionListener(e -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> cb = (JComboBox<String>) e.getSource();
            if (cb.getSelectedIndex() == 0) elementType = Imgproc.CV_SHAPE_ELLIPSE;
            else if (cb.getSelectedIndex() == 1) elementType = Imgproc.CV_SHAPE_RECT;
            else if (cb.getSelectedIndex() == 2) elementType = Imgproc.CV_SHAPE_CROSS;
            update();
        });
        ctrlPanel.add(elementTypeBox, BorderLayout.AFTER_LINE_ENDS);

        JComboBox<String> morphOpBox = new JComboBox<>(MORPH_OP);
        morphOpBox.addActionListener(e -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> cb = (JComboBox<String>) e.getSource();
            morphOpType = MORPH_OP_TYPE[cb.getSelectedIndex()];
            update();
        });
        ctrlPanel.add(morphOpBox, BorderLayout.LINE_END);

        JSlider slider = new JSlider(0, MAX_KERNEL_SIZE, 0);
        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            kernelSize = source.getValue();
            update();
        });

        outerPanel.add(ctrlPanel);
        outerPanel.add(new JLabel("Kernel size: 2n + 1"));
        outerPanel.add(slider);
        pane.add(outerPanel, BorderLayout.PAGE_START);

        imgLabel = new JLabel(new ImageIcon(img));
        imgLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imgLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String path = fileInfo.checkName(
                        fileInfo.getDirectory()
                                + fileInfo.getName()
                                + "_["
                                + MORPH_OP[morphOpBox.getSelectedIndex()]
                                + '_'
                                + ELEMENT_TYPE[elementTypeBox.getSelectedIndex()]
                                + '_'
                                + slider.getValue()
                                + ']'
                        , fileInfo.getType()
                );
                fileInfo.saveImageToDisk(path, matImgDst);
                System.out.println("Saved");
            }
        });
        pane.add(imgLabel, BorderLayout.CENTER);
    }

    private void update() {
        Mat element = Imgproc.getStructuringElement(
                elementType,
                new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize)
        );
        Mat grayMat = new Mat();
        Imgproc.cvtColor(matImgSrc, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grayMat, grayMat, new Size(3, 3));

        Imgproc.morphologyEx(grayMat, matImgDst, morphOpType, element);
        Image img = HighGui.toBufferedImage(matImgDst);
        imgLabel.setIcon(new ImageIcon(img));
        frame.repaint();
    }

    public static void main(String[] args) {
        // Load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(() -> new MoreMorphologyTransformations());
    }
}
