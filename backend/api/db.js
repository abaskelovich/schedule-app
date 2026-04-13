const fs = require('fs');
const path = require('path');

const dbPath = path.join(process.cwd(), 'schedules.json');

function readData() {
  try {
    if (fs.existsSync(dbPath)) {
      return JSON.parse(fs.readFileSync(dbPath, 'utf8'));
    }
  } catch (e) {
    console.error('Error reading data:', e);
  }
  return [];
}

function writeData(data) {
  fs.writeFileSync(dbPath, JSON.stringify(data, null, 2));
}

let schedules = readData();
let nextId = schedules.length > 0 ? Math.max(...schedules.map(s => s.id)) + 1 : 1;

module.exports = {
  getAll: () => schedules.sort((a, b) => {
    if (a.date !== b.date) return a.date.localeCompare(b.date);
    return a.time.localeCompare(b.time);
  }),
  
  getById: (id) => schedules.find(s => s.id === parseInt(id)),
  
  create: (schedule) => {
    const newSchedule = {
      id: nextId++,
      ...schedule,
      created_at: new Date().toISOString()
    };
    schedules.push(newSchedule);
    writeData(schedules);
    return newSchedule;
  },
  
  update: (id, schedule) => {
    const index = schedules.findIndex(s => s.id === parseInt(id));
    if (index !== -1) {
      schedules[index] = { ...schedules[index], ...schedule, id: parseInt(id) };
      writeData(schedules);
      return schedules[index];
    }
    return null;
  },
  
  delete: (id) => {
    const index = schedules.findIndex(s => s.id === parseInt(id));
    if (index !== -1) {
      schedules.splice(index, 1);
      writeData(schedules);
      return true;
    }
    return false;
  }
};
