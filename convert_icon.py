from PIL import Image

def convert_png_to_ico(png_path, ico_path):
    img = Image.open(png_path)
    icon_sizes = [(16,16), (32, 32), (48, 48), (64,64), (128, 128), (256, 256)]
    img.save(ico_path, format='ICO', sizes=icon_sizes)

if __name__ == '__main__':
    convert_png_to_ico('src/main/resources/icon.png', 'src/main/resources/icon.ico')
