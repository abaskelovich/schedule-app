const db = require('./db');

module.exports = async function handler(req, res) {
  const { method, query, body } = req;
  const id = query.id;

  if (method === 'GET') {
    if (id) {
      const schedule = db.getById(id);
      return schedule ? res.status(200).json(schedule) : res.status(404).json({ error: 'Not found' });
    }
    const schedules = db.getAll();
    return res.status(200).json(schedules);
  }

  if (method === 'POST') {
    const { title, description, time, date } = body;
    if (!title || !time || !date) {
      return res.status(400).json({ error: 'Title, time and date are required' });
    }
    const newSchedule = db.create({ title, description: description || '', time, date });
    return res.status(201).json(newSchedule);
  }

  if (method === 'PUT') {
    if (!id) {
      return res.status(400).json({ error: 'ID is required' });
    }
    const { title, description, time, date } = body;
    const updated = db.update(id, { title, description: description || '', time, date });
    return updated ? res.status(200).json(updated) : res.status(404).json({ error: 'Not found' });
  }

  if (method === 'DELETE') {
    if (!id) {
      return res.status(400).json({ error: 'ID is required' });
    }
    const deleted = db.delete(id);
    return deleted ? res.status(200).json({ success: true }) : res.status(404).json({ error: 'Not found' });
  }

  return res.status(405).json({ error: 'Method not allowed' });
}
