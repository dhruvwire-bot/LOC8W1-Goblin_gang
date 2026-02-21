import subprocess
import sys
import os


def check_ffmpeg():
    """Check if ffmpeg is installed."""
    try:
        subprocess.run(["ffmpeg", "-version"], capture_output=True, check=True)
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        return False


def convert_mp3_to_wav(mp3_path: str) -> str:
    """
    Convert an MP3 file to WAV format:
    - 16000 Hz sample rate
    - Mono channel
    - 16-bit PCM (LINEAR16)
    Saves output WAV in the same folder as the input MP3.
    Returns the path to the output WAV file.
    """
    if not os.path.exists(mp3_path):
        raise FileNotFoundError(f"File not found: {mp3_path}")

    if not mp3_path.lower().endswith(".mp3"):
        raise ValueError(f"Expected an .mp3 file, got: {mp3_path}")

    # Build output path: same folder, same name, .wav extension
    base = os.path.splitext(mp3_path)[0]
    wav_path = base + "_16k.wav"

    command = [
        "ffmpeg",
        "-y",                  # Overwrite output if exists
        "-i", mp3_path,        # Input file
        "-ar", "16000",        # Sample rate: 16kHz
        "-ac", "1",            # Mono channel
        "-sample_fmt", "s16",  # 16-bit PCM
        wav_path               # Output file
    ]

    print(f"Converting: {mp3_path}")
    print(f"Output:     {wav_path}")

    result = subprocess.run(command, capture_output=True, text=True)

    if result.returncode != 0:
        raise RuntimeError(f"ffmpeg error:\n{result.stderr}")

    size_kb = os.path.getsize(wav_path) / 1024
    print(f"Done! File size: {size_kb:.1f} KB\n")

    return wav_path


def convert_all_in_folder(folder: str):
    """Convert all MP3 files in the given folder."""
    mp3_files = [
        os.path.join(folder, f)
        for f in os.listdir(folder)
        if f.lower().endswith(".mp3")
    ]

    if not mp3_files:
        print(f"No MP3 files found in: {folder}")
        return

    print(f"Found {len(mp3_files)} MP3 file(s)\n")

    success, failed = [], []

    for mp3 in mp3_files:
        try:
            wav = convert_mp3_to_wav(mp3)
            success.append(wav)
        except Exception as e:
            print(f"FAILED: {mp3} → {e}\n")
            failed.append(mp3)

    # Summary
    print("=" * 40)
    print(f"Converted:  {len(success)} file(s)")
    print(f"Failed:     {len(failed)} file(s)")
    if success:
        print("\nOutput files:")
        for f in success:
            print(f"  ✓ {f}")


# ── Entry point ──────────────────────────────────────────────────────────────

if __name__ == "__main__":

    # Check ffmpeg first
    if not check_ffmpeg():
        print("ERROR: ffmpeg is not installed or not in PATH.")
        print("Install it with:  winget install ffmpeg")
        print("Then restart your terminal and try again.")
        sys.exit(1)

    # Usage:
    #   python convert_audio.py                    → converts all MP3s in current folder
    #   python convert_audio.py audio.mp3          → converts a single file
    #   python convert_audio.py C:\path\to\folder  → converts all MP3s in given folder

    if len(sys.argv) == 1:
        # No argument → convert everything in current folder
        convert_all_in_folder(os.getcwd())

    elif len(sys.argv) == 2:
        target = sys.argv[1]

        if os.path.isdir(target):
            convert_all_in_folder(target)

        elif os.path.isfile(target):
            try:
                convert_mp3_to_wav(target)
            except Exception as e:
                print(f"ERROR: {e}")
                sys.exit(1)

        else:
            print(f"ERROR: '{target}' is not a valid file or folder.")
            sys.exit(1)

    else:
        print("Usage:")
        print("  python convert_audio.py                  # all MP3s in current folder")
        print("  python convert_audio.py file.mp3         # single file")
        print("  python convert_audio.py C:\\path\\folder  # all MP3s in folder")
        sys.exit(1)