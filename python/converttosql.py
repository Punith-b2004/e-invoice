import os
import requests
from flask import Flask, request, jsonify

app = Flask(__name__)

from dotenv import load_dotenv
load_dotenv()
LLAMA_URL = os.getenv("LLAMA_URL") 
API_KEY = os.getenv("LLAMA_API_KEY") 
LLAMA_MODEL = os.getenv("LLAMA_MODEL")  

SYSTEM_PROMPT = """You are an expert SQL assistant. 
Your job is to convert natural language questions into correct, executable SQL queries.
- Only output the SQL query.
- Never explain anything.
- Never add markdown (no ```sql).
- Never say "Sure" or "Here is".
- Do not include quotes or extra text.
- Use proper table and column names based on common sense.
- Assume standard SQL (PostgreSQL/MySQL compatible).
- Always use SELECT (never CREATE, INSERT, etc. unless explicitly asked).

Examples:

User: How many users are there?
SQL: SELECT COUNT(*) FROM users;

User: Show me all customers from Germany ordered by registration date.
SQL: SELECT * FROM customers WHERE country = 'Germany' ORDER BY registration_date;

User: What is the average order value in 2024?
SQL: SELECT AVG(amount) FROM orders WHERE YEAR(order_date) = 2024;

Now generate the SQL query for the user question.
"""

def query_llm(prompt: str) -> str:
    
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": f"Question: {prompt}\nGenerate only the SQL query:"}
    ]
    
    payload = {
        "model": LLAMA_MODEL,
        "messages": messages,
        "max_tokens": 512,
        "temperature": 0.1,
        "top_p": 0.95,
        "stop": ["User:", "Question:", "\n\n", "[/INST]"]
    }

    headers = {
        "Content-Type": "application/json"
    }
    if API_KEY:
        headers["Authorization"] = f"Bearer {API_KEY}"

    try:
        # Use chat completions endpoint for Maverick
        chat_url = LLAMA_URL
        response = requests.post(chat_url, json=payload, headers=headers, timeout=60)
        response.raise_for_status()
        result = response.json()

        # Extract response (OpenAI format)
        sql = result.get("choices", [{}])[0].get("message", {}).get("content", "").strip()
        sql = sql.replace("```sql", "").replace("```", "").strip()
        sql = " ".join(line.strip() for line in sql.splitlines() if line.strip())
        sql = sql.strip()



        return sql

    except Exception as e:
        print(f"Error calling LLM: {e}")
        return "ERROR: Could not generate SQL"
@app.route('/generate-sql', methods=['POST'])
def generate_sql():
    data = request.get_json()
    user_query = data.get('query', '').strip()

    if not user_query:
        return jsonify({"error": "Query is required"}), 400

    # Final prompt sent to model
    full_user_prompt = f"User: {user_query}\nSQL:"

    sql_query = query_llm(full_user_prompt)

    return jsonify({
        "query": user_query,
        "sql": sql_query
    })

if __name__ == '__main__':
    print(f"Starting Flask API...")
    print(f"Model: {LLAMA_MODEL}")

    print(f"Backend URL: {LLAMA_URL}")
    app.run(host='0.0.0.0', port=5000, debug=False)