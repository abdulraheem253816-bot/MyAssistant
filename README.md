# MyAssistant — Apka Siri-jesa Voice Assistant

## Ye kya karta hai
- App ON karne par ek background service (foreground service, notification bar mein dikhegi) chalu ho jati hai
- Ye continuously sunti hai aur jab aap "**Hey Assistant**" bolte hain, agli baat ko command samajh kar execute karti hai
- Commands: app kholna, call karna, SMS bhejna, web search, alarm lagana

## Kaise use karein
Wake word bolne ke baad, foran command bolein, jese:
- "Hey Assistant" ... phir ... "open whatsapp"
- "Hey Assistant" ... phir ... "call 03001234567"
- "Hey Assistant" ... phir ... "search karachi weather"
- "Hey Assistant" ... phir ... "set alarm 7 am"

## Build kaise karein (zaroori)
Main ye code seedha aapke phone par install nahi kar sakta — ye source code hai jo aapko **Android Studio** mein compile karna hoga:

1. [Android Studio](https://developer.android.com/studio) install karein (free hai)
2. Ye poora `MyAssistant` folder open karein: File → Open → is folder ko select karein
3. Gradle sync hone dein (thora time lagega, internet chahiye)
4. Apna phone USB se connect karein (Developer Options → USB Debugging ON), ya emulator use karein
5. Green "Run" button dabayein — app phone par install ho jayegi

## Permissions
Pehli baar app kholne par ye permissions maangega:
- Microphone (sunne k liye)
- Call, SMS, Contacts (commands execute karne k liye)
- Battery optimization se exempt karne ki request (taake service band na ho)

Sab allow karna zaroori hai warna commands kaam nahi karengi.

## ⚠️ Zaroori Limitations (imaandari se bata raha hoon)
1. **Bilkul Siri jesa "hamesha on, phone locked ho tab bhi" nahi hoga.** Android khud restrict karta hai — screen off/Doze mode mein background listening rukti ya slow ho jati hai. Battery optimization exempt karne se behtar hota hai lekin 100% guarantee nahi.
2. **Free wake-word detection** (is code mein) Android ka built-in speech recognizer use karta hai — iske liye thori internet connectivity chahiye hoti hai (Google's recognizer). Agar aap fully offline, zyada reliable wake-word chahte hain, to **Picovoice Porcupine** jesi paid/free-tier SDK integrate karni hogi — wo zyada battery-efficient aur offline hai.
3. Kuch phone brands (Xiaomi, Oppo, Vivo) apni "aggressive battery saver" ki wajah se background apps ko khud band kar dete hain — Settings mein us app ko "no restrictions" / "autostart allowed" karna hoga.
4. Google Play Store par publish karna chahein to policy review hogi (background mic access sensitive category hai).

## Naye commands add karna
`CommandHandler.kt` file mein `when` block mein naya condition add karein — jese "flashlight on karo" ke liye actual torch-toggle code.

---
Agar chahen to main aapko:
- Offline/Porcupine wake-word integration
- WhatsApp par specific contact ko message bhejna
- Flashlight/WiFi/Bluetooth toggle commands
bhi add kar sakta hoon — bas bata dein.
