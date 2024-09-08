## What is GrimGuardian
GrimGuardian is a plugin that depends on GrimAC to provide or modify additional features. This plugin works only on Bukkit servers based on PaperMC and requires PacketEvents Plugin.

   ### The Purpose 
   **GrimGuardian** is to ensure that hackers gain absolutely no advantage.

## Complete FastBreak Prevention

Currently, Minecraft's block-breaking system operates on the client side. While this has the advantage of not being affected by server latency, it is also the most easily exploited system for cheats. Hack clients can send a "mining start" signal to the server and then send a "mining stop" signal before the block is actually destroyed, causing the server to accept this manipulation.

To address this issue, the block-breaking system will be switched from client-side operation to server-side operation. This process ensures that bypassing from the client side is absolutely impossible, thereby completely blocking FastBreak and similar cheats. The system will function exactly the same as in vanilla Minecraft.

[Watch this video](https://youtu.be/NTJbgVO5fSw?si=TqBT-qnuaLHNZG9W&t=5)