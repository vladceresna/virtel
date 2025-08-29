<p align="center">
<img src='https://i.postimg.cc/d0WWH1MR/a2c68588eab85ad2459788d74bd36534.webp' border='0'/>
<h1 align="center"><u>Virtel</u>: Your best virtual enviroment</h1>
</p>

<p align="center">
</p>

-------------
`❓` Virtel is the fastest, the most secure and safe virtual enviroment. Best suited for mathematical calculations, development or deployment of applications and servers, though can be used for any purpose. We call it the **operating system**.

`🎯` What is the point of Virtel? Our goal is pretty simple: instead of writing software for different platforms separately or adding compatibility with other OS(es) later, we want people to write programs via **Virtel** - this **allows your code to run on practically any machine**, as Virtel is specifically made to be compatible with most platforms.

Please see last releases on Releases page and join our Discord server: https://discord.gg/hNSyTvuy2v .

--------------
### We need help!
`🫂` Virtel is currently in the stage of early development, which means that as for now, most of the desired functionality is not here yet. For that reason, we need **your help** to develop Virtel.

If you know **Compose Multiplatform**, you can help us to develop Virtel itself. At current stage we lack an **IDE to write programs that will run on Virtel**, and there aren't any programming languages **that would compile into <u>Steps</u>**.

-----------
### What is "Steps"?
`❓` **Steps** is a programming language made specifically to run on Virtel. As have been said before, Steps also lacks lots of functionality, but is constantly improved - creating programs in Steps isn't that far away from reality. The file extension that Steps uses is pretty straightforward: `.steps`. The same thing can be said about the language itself, as the syntax is pretty easy to learn. If you don't like Steps, in the near future you will be able to write code via another language that compiles into Steps.

------------
### Quick overview of Steps
`🔬` In the future, there will be a dedicated Steps documentation, where you will be able to read and see lots of examples. **This section exists only for the sole purpose of providing some basic information about the Steps language**, as the documentation **hasn't been written yet**.

`🛠️` List of currently available commands:
* `sys out {name of the variable or any text}`
* `var set {var name} {var value}`
* `var get {var name}`
* `var del {name of the variable}`
* `bin run {name of the .steps script}`
* `mat plus {num as text or var name} {num as text or var name} {name of the var to store the result}`
* `mat min {num as text or var name} {num as text or var name} {name of the var to store the result}`
* `mat mult {num as text or var name} {num as text or var name} {name of the var to store the result}`
* `mat div {num as text or var name} {num as text or var name} {name of the var to store the result}`
* other commands for every needs

### Different code examples:
#### Basic example, working with `bin run`:
`start.steps` is always a starting point for any Steps application. In this example we also jump to the `run.steps` file.

Path of the file (may depend on the system): `C:/Virtel/apps/vladceresna.virtel.launcher/bin/start.steps`

Script in `start.steps`:
```
var set "Hello!" greet;
csl write greet;
var set "Hi!" greet;
csl write greet;
var del greet;
csl write greet;
run one "/run.steps";
```
Script in `run.steps`:
```
csl write "Hello from run";
csl write "This is run";
```

#### Another example, working with math commands:
This example briefly shows how you can use mathematics in Steps.

Steps Code:
```
var set a 5;
var set b 5;
mat plus a b c;
csl write c;
```
Output of the code:
```
10
```

---------
### Structure

VAR - Virtel Archive - format for distributing apps

STEPS - Step`s - native virtel apps developing and running format

STEPS:
start.steps:
```
csl write "Hello world";
```
---------
### List of currently implemented modules:
- [x] csl (Console, command line)
- [x] ref (References on vars)
- [x] var (Vars)
- [x] lst (Lists)
- [x] mat (Math and Random operations)
- [x] str (String operations (ex. cut))
- [x] bln (Boolean operations)
- [x] run (if, while)
- [x] sys (System data (ex. all programs))
- [x] fls (Files)
- [x] scr (Operate screen components)
- [ ] dgm (Diagrams)
- [x] srv (Server on selected ports)
- [x] clt (Client for urls)
- [ ] wss (WebSockets)
- [ ] blt (Bluetooth)
- [ ] ppt (Perspective projection (3D in ui))
- [ ] ort (Orthogonal projection (3D in ui))
- [ ] ard (AR)
- [ ] vrd (VR)
- [ ] anm (Interface animations)
- [x] tts (Text To Speech)
- [ ] stt (Speech To Text)
- [ ] mcr (Microphone)
- [x] spr (Speaker)
- [x] llm (Pino helper)
- [ ] nnt (Neuronets)
- [ ] cmr (Camera)
- [ ] snr (Sensors of device)
- [ ] cnt (Connect&Sync with Virtel`s on other machines)


### List of currently supported platforms:
* [x] Windows
* [x] Linux
* [x] Android
* [ ] MacOS
* [ ] iOS
* [ ] Redox
* [ ] Native (OS)

---------
### Our Git Flow

<img src='https://miro.medium.com/v2/resize:fit:828/format:webp/1*q1Q_vY3tA8u8CVaiqr-rxA.png' border='0'/>

---------
### Project Structure
This is a Kotlin Multiplatform project targeting Android, iOS, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re shDon't even tryaring your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)

# How to build Virtel?
Don't even try :)

But okay, if you still want to dive into this mess, please, I'll help you do it :)

Although, of course, I don't fully understand how it works myself, but still.

## You need to have the following **installed**:

### If you on Linux or maybe on Windows:

- JDK, for example [Liberica](https://bell-sw.com/pages/downloads/#jdk-21-lts) (Java Development Kit) (You can do it via SDK MAN)
- [Android Studio](https://developer.android.com/studio) (Yes, even for desktop builds)
- [Rust Toolchain](https://www.rust-lang.org/tools/install)

Next, via Android Studio, ensure you have installed:

- Android NDK
- Android SDK

### If you on MacOS

I don't know, what you need to have installed, because I don't have Macbook, so you could need the same toolchains as for Linux, but yet you will need to have XCode or something.

### Common recommendations

**Use LLM!!! Use LLM!!!**

Every error just share to LLM as ChatGPT.

Sometimes you may need to install some libraries.
If you have installed some system library and this have been helpful for your version of your operating system - create issue and we will add it to checklist on this page.
