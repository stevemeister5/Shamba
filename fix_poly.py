import sys  
file_path = 'shamba-smart/app/src/main/java/com/shambasmart/map/FarmMapScreen.kt'  
with open(file_path, 'r', encoding='utf-8') as f:  
    lines = f.readlines()  
start = 255  
end = 282  
new_section = [  
'            // Polygon drawing overlay\n',  
