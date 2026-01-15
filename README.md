# Spring AI Demo

This project demonstrates various Spring AI capabilities including RAG (Retrieval Augmented Generation), Function Calling, Structured Output, and response optimization.

## Prerequisites

* Java 21
* Google Gemini API Key

## Configuration

Set your API key in an environment variable named `GEMINI_API_KEY` before running the application.

## API Examples

Once the application is running (default port: 8080), you can test the following endpoints by pasting these URLs into your browser.

### 1. Basic RAG (Retrieval Augmented Generation)
Asks a question using the course syllabus as context. The AI is instructed to answer based only on the provided material.

http://localhost:8080/ask?query=What+are+the+prerequisites+for+this+course?

### 2. Streaming Response (Server-Sent Events)
Stream the response text-by-text. This example asks a long question to demonstrate the streaming effect clearly.

http://localhost:8080/ask-stream?query=Explain+the+rules+of+Quidditch+in+extreme+detail,+describing+every+ball+(Quaffle,+Bludger,+Snitch),+the+positions+of+the+players,+how+points+are+scored,+and+what+happens+during+Harry's+first+match.

### 3. Structured Output (JSON Extraction)
Extracts specific details (Course Name, ID, Coordinator, Grading) from the syllabus and returns them as a structured JSON object.

http://localhost:8080/course-info

### 4. Structured Output (Application Usage)
Uses the structured data from the endpoint above to generate a pre-formatted summary string in Java.

http://localhost:8080/course-summary

### 5. Function Calling (Tools)
The AI detects the intent to calculate a grade, pauses execution, calls the Java `calculateFinalGrade` method, and uses the result to answer.

http://localhost:8080/calculate?query=I+got+90+on+the+exam,+80+on+the+project,+and+100+on+homework.+Did+I+pass?

### 6. Optimized Response (CSV Interceptor)
Similar to the calculation above, but uses an interceptor to convert the tool's output from JSON to CSV before sending it back to the AI. This reduces token usage.

http://localhost:8080/calculate-optimized?query=I+got+90+on+the+exam,+80+on+the+project,+and+100+on+homework.+Did+I+pass?

### 7. Prompt Templates
Uses an external template file (`course-assistant.st`) to structure the prompt, adopting a specific persona ("Captain Codebeard").

http://localhost:8080/ask-with-template?query=What+is+the+attendance+policy?
