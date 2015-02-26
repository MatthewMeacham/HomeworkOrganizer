Orgnizr - Online Homework Organiser
===================================

The official Orgnizr GitHub repository. Orgnizr is an online homework organiser that runs on the play framework. It is being developed by Matthew Meacham and Ross MacPhee

### Todo List

- [x] Finish Page Templates
- [x] Update Blog
- [x] Update Colour Scheme
- [ ] Allow Complete MySQL Compatibility
- [ ] Complete UI
- [ ] Allow people to add homework and classes
- [ ] Add Tests

Installing Play Framework
=========================

### Prerequisites
To run the Play framework, you need [JDK 6 or later.](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

> If you are using MacOS, Java is built-in. If you are using Linux, make sure to use either the Sun JDK or OpenJDK (and not gcj, which is the default Java command on many Linux distros). If you are using Windows, just download and install the latest JDK package.

Be sure to have the `java` and `javac` commands in the current path (you can check this by typing `java -version` and `javac -version` at the shell prompt).

### Install Activator

Play is distributed through a tool called [Typesafe Activator](http://typesafe.com/activator). Typesafe Activator provides the build tool (sbt) that Play is built on, and also provides many templates and tutorials to help get you started with writing new applications.

Download the latest [Activator distribution](https://typesafe.com/platform/getstarted) and extract the archive to a location where you have both read **and write access**. (Running `activator` writes some files to directories within the distribution, so don’t install to `/opt`, `/usr/local` or anywhere else you’d need special permission to write to.)

### Add the activator script to your PATH

For convenience, you should add the Activator installation directory to your system `PATH`. On UNIX systems, this means doing something like:

```
export PATH=$PATH:/relativePath/to/activator
```

On Windows you’ll need to set it in the global environment variables. This means update the `PATH` in the environment variables and don’t use a path with spaces.

> If you’re on UNIX, make sure that the `activator` script is executable.

> Otherwise do a:
`bash chmod a+x activator`

> If you’re behind a proxy make sure to define it with `set HTTP_PROXY=http://<host>:<port>` on Windows or `export HTTP_PROXY=http://<host>:<port>` on UNIX.

### Check that the activator command is available

From a shell, launch the `activator -help` command.
> $ activator -help

If everything is properly installed, you should see the basic help:
![Basic help on terminal](https://www.playframework.com/documentation/2.3.x/resources/manual/gettingStarted/images/activator.png)

You are now ready to use the Orgnizr application.

Running Play
============

In terminal or console type: `play`

You should see somthing like this appear:
![Play Terminal](https://aliquamgames.com/theme/img/play.png)

Now we need to change directories. In terminal or console type cd /path to Orgnizr folder/

Next type `play run`

Once you see the console display theese messages:

> --- (Running the application from SBT, auto-reloading is enabled) ---

> [info] play - Listening for HTTP on /0:0:0:0:0:0:0:0:9000

> (Server started, use Ctrl+D to stop and go back to the console...)

Go to: `localhost:9000` in a browser of your choice, most preferably google chrome. Wait a few seconds (10 - 15) until you see this message appear:

![Apply SQL Script](https://aliquamgames.com/theme/img/sql.png)


Here are the usernames and passwords you can use to log in:

| Email  | Password |
| ------------- | ------------- |
| bob@gmail.com  | hunter2  |
| billy@gmail.com  | hunter2  |
| rachel@gmail.com  | hunter2  |

Extra Information
=================

### Copyright
All assets and images (excluding the play framework and activator images and media) are ©Aliquam Game Studios - 2014

### Biblography
[Play Framework Documentation](https://www.playframework.com/documentation/2.3.x/Home)
