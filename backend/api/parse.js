const https = require('https');

const systemPrompt = `
You are a schedule parser. Extract schedule details from user text.
Return ONLY a JSON object with these fields (no other text):
{
  "title": "event name",
  "description": "optional description",
  "time": "HH:MM format",
  "date": "YYYY-MM-DD format"
}

If time is not specified, use "09:00".
If date is not specified, use today.

Examples:
- "Meeting with John tomorrow at 3pm" -> {"title": "Meeting with John", "description": "", "time": "15:00", "date": "tomorrow's date"}
- "Gym workout every day at 7am" -> {"title": "Gym workout", "description": "every day", "time": "07:00", "date": "today"}
- "Call mom" -> {"title": "Call mom", "description": "", "time": "09:00", "date": "today"}
`;

module.exports = async function handler(req, res) {
  console.log('--- Parse Intent Start ---');
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  const { input } = req.body || {};
  if (!input) {
    console.error('No input provided in request body');
    return res.status(400).json({ error: 'Input is required' });
  }

  const apiKey = process.env.OPENAI_API_KEY;
  if (!apiKey) {
    console.error('OPENAI_API_KEY is missing');
    return res.status(500).json({ error: 'OpenAI API Key is not configured' });
  }

  const todayStr = new Date().toISOString().split('T')[0];
  const userPrompt = `User input: "${input}"\nCurrent date: ${todayStr}\nParse this and return JSON with title, description, time (HH:MM), and date (YYYY-MM-DD).`;

  console.log(`Sending to OpenAI: ${input}`);

  const data = JSON.stringify({
    model: "gpt-4o-mini",
    messages: [
      { role: "system", content: systemPrompt },
      { role: "user", content: userPrompt }
    ],
    max_tokens: 500
  });

  const options = {
    hostname: 'api.openai.com',
    port: 443,
    path: '/v1/chat/completions',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${apiKey}`,
      'Content-Length': Buffer.byteLength(data)
    }
  };

  return new Promise((resolve) => {
    const openaiReq = https.request(options, (openaiRes) => {
      let body = '';
      openaiRes.on('data', (chunk) => body += chunk);
      openaiRes.on('end', () => {
        try {
          if (openaiRes.statusCode !== 200) {
            console.error('OpenAI Error HTTP Status:', openaiRes.statusCode);
            console.error('OpenAI Error Body:', body);
            res.status(openaiRes.statusCode).json({ error: 'OpenAI API error', detail: body });
            return resolve();
          }

          const response = JSON.parse(body);
          const content = response.choices[0].message.content;
          console.log('AI response content:', content);
          
          let jsonStr = content.trim();
          if (jsonStr.startsWith('```json')) {
            jsonStr = jsonStr.substring(7, jsonStr.length - 3).trim();
          } else if (jsonStr.startsWith('```')) {
            jsonStr = jsonStr.substring(3, jsonStr.length - 3).trim();
          }

          const result = JSON.parse(jsonStr);
          console.log('Parsed JSON:', result);
          
          // Обработка относительных дат
          if (result.date === 'tomorrow') {
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            result.date = tomorrow.toISOString().split('T')[0];
          } else if (result.date === 'today') {
            result.date = todayStr;
          }

          res.status(200).json(result);
          resolve();
        } catch (e) {
          console.error('Processing error:', e);
          res.status(500).json({ error: 'Internal processing error', detail: e.message });
          resolve();
        }
      });
    });

    openaiReq.on('error', (e) => {
      console.error('HTTPS request error:', e);
      res.status(500).json({ error: 'Connection to OpenAI failed', detail: e.message });
      resolve();
    });

    openaiReq.write(data);
    openaiReq.end();
  });
};
