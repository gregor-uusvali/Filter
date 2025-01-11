# Filter

A Java-based command-line utility for applying various image filters and transformations to images. This tool supports JPEG and PNG formats, offering multiple filtering options including grayscale conversion, edge detection, and ASCII art generation.

## Features

- **Grayscale Filter**: Converts images to grayscale by averaging RGB values
- **Edge Detection**: Implements Sobel operator for edge detection
- **ASCII Art**: Transforms images into ASCII art with customizable width and brightness levels
- Outputs both console ASCII art and a PNG image of the ASCII conversion

## Usage

bash: ```java Filter <filter_type> <image_file>```

Filter Types

- grayscale: Converts image to grayscale
- edges: Applies edge detection
- ascii: Converts image to ASCII art

Supported File Formats

JPEG (.jpg, .jpeg)
PNG (.png)

Output

Processed images are saved with the filter name prefixed to the original filename
ASCII art is both displayed in the console and saved as 'ascii_art.png'

Technical Details

Implements custom image matrix manipulation for filters
Uses Java AWT and ImageIO for image processing
Preserves alpha channel in supported formats
Includes error handling for file operations and format validation