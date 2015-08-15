local component = require("component")

if not component.isAvailable("warpdriveRadar") then
  print("No radar detected")
  return
end

local radar = component.warpdriveRadar

local argv = { ... }
if #argv ~= 1 then
  print("Usage: ping <scanRadius>")
  return
end
local radius = tonumber(argv[1])

if radius < 1 or radius > 9999 then
  print("Radius must be between 1 and 9999")
  return
end

energy, energyMax = radar.getEnergyLevel()
if energy < radius * radius then
  print("Low energy level... (" + energy + "/" + radius * radius + ")")
  return
end
radar.scanRadius(radius)
os.sleep(0.5)

print("Scanning...")

local seconds = 0
local count = nil
repeat
  count = radar.getResultsCount()
  os.sleep(1)
  seconds = seconds + 1
until (count ~= nil and count ~= -1) or seconds > 10
print("took " .. seconds .. " seconds")

if count ~= nil and count > 0 then
  for i=0, count-1 do
    freq, x, y, z = radar.getResult(i)
    print("Ship '" .. freq .. "' @ (" .. x .. " " .. y .. " " .. z .. ")")
  end
else
  print("Nothing was found =(")
end

print("")
