local component = require("component")
local computer = require("computer")
local term = require("term")
radius = 500
scale = 50

if not term.isAvailable() then
  computer.beep()
  return
end
if not component.isAvailable("warpdriveRadar") then
  computer.beep()
  print("No radar detected")
  return
end
radar = component.warpdriveRadar

w, h = component.gpu.getResolution()

term.clear()

function textOut(x, y, text, fg, bg)
  if term.isAvailable() then
    local w, h = component.gpu.getResolution()
    if w then
      component.gpu.setBackground(bg)
      component.gpu.setForeground(fg)
      component.gpu.set(x, y, text)
      component.gpu.setBackground(0x000000)
    end
  end
end

function drawBox(x, y, width, height, color)
  if term.isAvailable() then
    local w, h = component.gpu.getResolution()
    if w then
      component.gpu.setBackground(color)
      component.gpu.fill(x, y, width, height, " ")
      component.gpu.setBackground(0x000000)
    end
  end
end

function translateXZ(oldX, oldZ, i)
  local x = radarX - oldX
  local z = radarZ - oldZ
  
  x = x / (radius / scale)
  z = z / (radius / scale)
  
  x = x + (w / 2)
  z = z + (h / 2)
  
  x = math.floor(x)
  z = math.floor(z)
  
  return x,z
end

function drawContact(x, y, z, name, color)
  local newX, newZ = translateXZ(x, z)
  
  textOut(newX, newZ, " ", 0x000000, color)
  textOut(newX - 3, newZ + 1, "[" .. name .. "]", 0xFFFFFF, 0x000000)
end

function scanAndDraw()
  local energy, energyMax = radar.getEnergyLevel()
  if (energy < radius * radius) then
    hh = math.floor(h / 2)
    hw = math.floor(w / 2)
    
    drawBox(hw - 5, hh - 1, 11, 3, 0xFF0000)
    textOut(hw - 4, hh, "LOW POWER", 0xFFFFFF, 0xFF0000)
    os.sleep(1)
    
    return 0
  end
  radar.scanRadius(radius)
  os.sleep(2)
  
  redraw()
  
  numResults = radar.getResultsCount()
  
  if (numResults ~= 0) then
    for i = 0, numResults-1 do
      freq, cx, cy, cz = radar.getResult(i)
      
      drawContact(cx, cy, cz, freq, 0xFF0000)
    end
  end
  
  drawContact(radarX, radarY, radarZ, "RAD", 0xFFFF00)
end

function redraw()
  drawBox(2, 1, w - 2, h - 1, 0x00FF00)
  
  drawBox(1, 1, w, 1, 0x000000)
  drawBox(1, 1, 1, h, 0x000000)
  drawBox(1, h, w, 1, 0x000000)
  drawBox(w, 1, w, h, 0x000000)
  
  textOut((w / 2) - 8, 1, "= Q-Radar v0.1 =", 0xFFFFFF, 0x000000)
  
  textOut(w - 3, 1, "[X]", 0xFFFFFF, 0xFF0000)
  
  local energy, energyMax = radar.getEnergyLevel()
  textOut(4, h, "Energy: " .. energy .. " EU | Scan radius: " .. radius, 0xFFFFFF, 0x000000)
end

radarX, radarY, radarZ = radar.pos()

while component.isAvailable("warpdriveRadar") do
  scanAndDraw()
end

term.clear()