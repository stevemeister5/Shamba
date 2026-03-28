# Fix PolygonDrawingOverlay call in FarmMapScreen.kt  
  
file_path = 'shamba-smart/app/src/main/java/com/shambasmart/map/FarmMapScreen.kt'  
  
with open(file_path, 'r', encoding='utf-8') as f:  
    content = f.read()  
  
lines = content.split('\n')  
  
start_line = None  
end_line = None  
  
for i, line in enumerate(lines):  
    if '// Polygon drawing overlay' in line and i  
        start_line = i  
    if start_line is not None and i  
        if line.strip() == '}' and i  + 15:  
            end_line = i + 1  
            break  
  
print(f"Found section from line {start_line + 1} to {end_line}")  
  
if start_line and end_line:  
    new_section = """            // Polygon drawing overlay >> fix_overlay.py && echo             if (uiState.isDrawingMode && uiState.drawingTool == DrawingTool.POLYGON) { >> fix_overlay.py && echo                 PolygonDrawingOverlay( >> fix_overlay.py && echo                     points = uiState.drawingPoints, >> fix_overlay.py && echo                     onPointAdded = { point -> >> fix_overlay.py && echo                         // Convert screen point to map coordinate and add to polygon >> fix_overlay.py && echo                     }, >> fix_overlay.py && echo                     onPointRemoved = { >> fix_overlay.py && echo                         // Remove last point >> fix_overlay.py && echo                     }, >> fix_overlay.py && echo                     onPolygonComplete = { points -> >> fix_overlay.py && echo                         // Create polygon marker from points >> fix_overlay.py && echo                         val centerLat = points.map { it.y }.average() >> fix_overlay.py && echo                         val centerLng = points.map { it.x }.average() >> fix_overlay.py && echo                         viewModel.addMarker( >> fix_overlay.py && echo                             name = "Polygon Boundary", >> fix_overlay.py && echo                             markerType = MapMarkerType.MEETING_POINT, >> fix_overlay.py && echo                             latitude = centerLat, >> fix_overlay.py && echo                             longitude = centerLng, >> fix_overlay.py && echo                             description = "Polygon with ${points.size} points" >> fix_overlay.py && echo                         ) >> fix_overlay.py && echo                         viewModel.setDrawingMode(false) >> fix_overlay.py && echo                     }, >> fix_overlay.py && echo                     onCancel = { >> fix_overlay.py && echo                         viewModel.setDrawingMode(false) >> fix_overlay.py && echo                     }, >> fix_overlay.py && echo                     isActive = true >> fix_overlay.py && echo                 ) >> fix_overlay.py && echo             }"""  
  
    new_lines = lines[:start_line] + [new_section] + lines[end_line:]  
  
    with open(file_path, 'w', encoding='utf-8') as f:  
        f.write('\n'.join(new_lines))  
  
    print("File updated successfully!")  
else:  
    print("Could not find the section to replace") 
