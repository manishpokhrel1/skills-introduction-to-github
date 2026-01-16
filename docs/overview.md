# What DAMI Is

DAMI is a wake-word–activated Android assistant that:

- Listens only after a wake word
- Understands commands and factual questions
- Uses rules + internet data
- Controls phone features with permission
- Explains what’s on the screen
- Speaks responses clearly

DAMI is not alive, not intelligent, not emotional. She is deterministic, honest, and reliable. That’s a strength.

## How DAMI Starts (Wake Word)

- Phone mic is active in a foreground service
- C++ wake engine listens for keyword (e.g. “Dami”)
- Engine does NOT record speech — it only detects a pattern and triggers Android

How it works:

- MFCC feature extraction
- Lightweight classifier
- No speech recognition
- Very low battery use

Result: “Dami” → DAMI wakes → now she listens

## How DAMI Hears You (Speech Input)

Once awake, Android `SpeechRecognizer` converts voice → text. Short commands only; it stops listening automatically.

Example: “Dami, who is Nikola Tesla”

## How DAMI Understands (Intent Routing)

DAMI does NOT think. She routes. Every sentence is matched to a category (intent). Examples:

- “Who is Elon Musk” → PERSON_LOOKUP
- “What is gravity” → DEFINITION
- “Tell me a joke” → JOKE
- “What’s the news” → NEWS
- “Where am I” → LOCATION
- “What is this” → SCREEN_CONTEXT
- “Call mom” → CALL
- “Send SMS to John” → SMS

If no intent matches: “I don’t have a reliable answer for that.” — intentional honesty.

## Where DAMI’s Answers Come From

1) Factual answers (Who/What): Wikipedia (first paragraph, stripped, shortened).
2) Jokes: public joke APIs, RSS joke feeds, filtered for safety.
3) News: RSS feeds; fetch latest headlines and short summaries (top 1–3).
4) Location: Android Location Services (requires permission).
5) Weather: public weather APIs, based on current location.

No opinions. No fake summaries.

## How DAMI Controls Your Phone

Only with permission. Capabilities include calls, SMS, launching apps, changing silent/vibrate, opening settings, and emergency triggers. All use standard Android intents and permission checks.

## How DAMI “Sees” the Screen

Not vision. An Accessibility Service reads the UI tree (app name, visible text, focused elements) and DAMI reports structure like a screen reader. No image understanding.

## Automation (Power Feature)

Supports rule-based automation: Trigger → Condition → Action. Examples: “Good night” → Silent + alarm; Battery < 20% → Power saver; “Emergency” → Call + SMS.

## How DAMI Speaks

Android Text-to-Speech with a neutral, calm voice; short, factual responses.

## What DAMI Will Refuse

Refuses opinions, philosophical/emotional advice, predictions, and anything implying sentience. Response: “I don’t have a factual answer for that.”

## Summary

DAMI is a wake-word–activated Android assistant that listens on command, uses rule-based intent detection, fetches real information from reliable internet sources (Wikipedia, news, jokes, weather), explains screen context using accessibility data, controls phone features with permission, and speaks short factual responses without pretending to think or feel.
