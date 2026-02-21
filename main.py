from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from google.cloud import speech
import asyncio
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Speech-to-Text Microservice", version="1.0.0")

# Initialize client once at startup
speech_client = speech.SpeechClient()

RECOGNITION_CONFIG = speech.RecognitionConfig(
    encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
    sample_rate_hertz=16000,
    language_code="hi-IN",                    # Primary: Hindi
    alternative_language_codes=["en-IN"],      # Fallback: English
    enable_automatic_punctuation=True,
    model="default",
)


@app.get("/health")
async def health():
    return {"status": "ok"}


@app.post("/speech-to-text")
async def speech_to_text(file: UploadFile = File(...)):
    # Validate file type
    if not file.filename.endswith(".wav"):
        raise HTTPException(status_code=400, detail="Only WAV files are supported.")

    try:
        audio_bytes = await file.read()

        if not audio_bytes:
            raise HTTPException(status_code=400, detail="Uploaded file is empty.")

        # Run blocking Google API call in thread pool to stay async-friendly
        transcript = await asyncio.get_event_loop().run_in_executor(
            None, _recognize, audio_bytes
        )

        logger.info(f"Transcribed: '{transcript}' from file: {file.filename}")
        return JSONResponse(content={"transcript": transcript, "file": file.filename})

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Speech recognition failed: {e}")
        raise HTTPException(status_code=500, detail=f"Speech recognition error: {str(e)}")


def _recognize(audio_bytes: bytes) -> str:
    """Synchronous Google STT call — runs in executor."""
    audio = speech.RecognitionAudio(content=audio_bytes)
    response = speech_client.recognize(config=RECOGNITION_CONFIG, audio=audio)

    if not response.results:
        return ""  # No speech detected — return empty, not an error

    # Concatenate all result transcripts
    transcript = " ".join(
        result.alternatives[0].transcript
        for result in response.results
        if result.alternatives
    )
    return transcript.strip()
