# HackUVA

## Inspiration
While many of us had musical training before college, it's hard to bring that kind of enjoyment to a busy academic environment. Additionally, not everyone has the money or space to afford a full-size instrument or even lessons. As programmers, we wanted to show how inexpensive playing a fully-featured instrument can be. 

## What it does
Hey Midi! goes beyond traditional instruments in that it is fully customizable, intuitive, and collaborative. Using the Leap Motion Sensor, the hand positions of you and your friends are translated into MIDI signals that can be used to drive any virtual instrument that you want. The affordable Leap Motion Sensor is all you need to begin your own musical journey; its intuitive and forgiving controls allow anyone to create music without having to worry about fingerings or form.

Hey Midi! is easy to pick up, but offers power users the ability to interface with any synthesizer in any way that they want. We wanted the Hey Midi! experience to be as inclusive as possible, which is why we included features that encourage collaboration and sharing. With multi-user support, you can duet with your friends or have a seasoned player guide your performance. What's more, you can record and share your compositions to our online platform with quick and easy voice commands. 

## How we built it

Hey Midi! is divided into 3 main components. The instrument component runs on a Java program that uses the Leap Motion API to translate gestures into music. The voice control component runs in the background, and utilizes the Sphinx4 Voice Recognition API to trigger the record and upload functions. Finally, a node.js web server hosted on SAP Cloud Platform hosts our MIDI uploads for the public to enjoy. 

## Challenges we ran into

- Leap motion driver issues
- Voice recognition dictionary has to be trained manually

## Accomplishments that we're proud of

- Implementing hotword recognition
- Leap motion visualizer 
- Virtual area-based knob and slider controls
- Deploying to SAP Cloud

## What we learned

- Commit often
- Improved our debugging pair programming
- How to divide and conquer big projects across multiple languages, APIs, and OS's

## What's next for Hey Midi!

- Cross platform support
- Better playback controls on the web
- User authentication for upload
- NLP improvements for voice UI
