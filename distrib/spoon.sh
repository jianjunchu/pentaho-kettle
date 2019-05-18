#!/bin/sh

# **************************************************
# ** Set these to the location of your mozilla
# ** installation directory.  Use a Mozilla with
# ** Gtk2 and Fte enabled.
# **************************************************

# set MOZILLA_FIVE_HOME=/usr/local/mozilla
# set LD_LIBRARY_PATH=/usr/local/mozilla

# Try to guess xulrunner location - change this if you need to
MOZILLA_FIVE_HOME=$(find /usr/lib -maxdepth 1 -name xulrunner-[0-9]* | head -1)
LD_LIBRARY_PATH=${MOZILLA_FIVE_HOME}:${LD_LIBRARY_PATH}
export MOZILLA_FIVE_HOME LD_LIBRARY_PATH

# Fix for GTK Windows issues with SWT
export GDK_NATIVE_WINDOWS=1

# Fix overlay scrollbar bug with Ubuntu 11.04
export LIBOVERLAY_SCROLLBAR=0



# **************************************************
# ** Init BASEDIR                                 **
# **************************************************

BASEDIR=`dirname $0`
CLASSPATH=$BASEDIR
CLASSPATH=$CLASSPATH:$BASEDIR/lib/ketl-1.0.jar
CLASSPATH=$CLASSPATH:$BASEDIR/libswt/jface.jar
CLASSPATH=$CLASSPATH:$BASEDIR/libswt/runtime.jar
CLASSPATH=$CLASSPATH:$BASEDIR/libswt/common.jar
CLASSPATH=$CLASSPATH:$BASEDIR/libswt/commands.jar
for f in `find $BASEDIR/libext`
do
  CLASSPATH=$CLASSPATH:$f
done

for f in `find $BASEDIR/lib`
do
  CLASSPATH=$CLASSPATH:$f
done

#echo "$PATH"
cd $BASEDIR
DIR=`pwd`
cd -

. "$DIR/set-pentaho-env.sh"

setPentahoEnv
cd $BASEDIR

# **************************************************
# ** Platform specific libraries ...              **
# **************************************************

LIBPATH="NONE"
STARTUP="-jar $BASEDIR/launcher/launcher.jar -Dorg.eclipse.swt.browser.DefaultType=mozilla"

case `uname -s` in 
	AIX)
		LIBPATH=libswt/aix/
		;;

	SunOS) 
		LIBPATH=libswt/solaris/
		;;

	Darwin)
    ARCH=`uname -m`
	OPT="-XstartOnFirstThread $OPT"
	case $ARCH in
		x86_64)
			if $($_PENTAHO_JAVA -version 2>&1 | grep "64-Bit" > /dev/null )
                            then
			  LIBPATH=libswt/osx64/
                            else
			  LIBPATH=libswt/osx/
                            fi
			;;

		i[3-6]86)
			LIBPATH=libswt/osx/
			;;

		*)	
			echo "I'm sorry, this Mac platform [$ARCH] is not yet supported!"
			echo "Please try starting using 'Data Integration 32-bit' or"
			echo "'Data Integration 64-bit' as appropriate."
			exit
			;;
	esac
	;;


	Linux)
	    ARCH=`uname -m`
		case $ARCH in
			x86_64)
				if $($_PENTAHO_JAVA -version 2>&1 | grep "64-Bit" > /dev/null )
                                then
				  LIBPATH=libswt/linux/x86_64/
                                else
				  LIBPATH=libswt/linux/x86/
                                fi

				;;

			i[3-6]86)
				LIBPATH=libswt/linux/x86/
				;;

			ppc)
				LIBPATH=libswt/linux/ppc/
				;;

			*)	
				echo "I'm sorry, this Linux platform [$ARCH] is not yet supported!"
				exit
				;;
		esac
		;;

	FreeBSD)
	    ARCH=`uname -m`
		case $ARCH in
			x86_64)
				LIBPATH=libswt/freebsd/x86_64/
				echo "I'm sorry, this FreeBSD platform [$ARCH] is not yet supported!"
				exit
				;;

			i[3-6]86)
				LIBPATH=libswt/freebsd/x86/
				;;

			ppc)
				LIBPATH=libswt/freebsd/ppc/
				echo "I'm sorry, this FreeBSD platform [$ARCH] is not yet supported!"
				exit
				;;

			*)	
				echo "I'm sorry, this FreeBSD platform [$ARCH] is not yet supported!"
				exit
				;;
		esac
		;;

	HP-UX) 
		LIBPATH=libswt/hpux/
		;;
	CYGWIN*)
		./Spoon.bat
		exit
		;;

	*) 
		echo Spoon is not supported on this hosttype : `uname -s`
		exit
		;;
esac 

export LIBPATH

if [ "$LIBPATH" != "NONE" ]
then
  for f in `find $BASEDIR/$LIBPATH -name '*.jar'`
  do
    CLASSPATH=$CLASSPATH:$f
  done
fi


# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 512m to higher values in case you run out of memory   **
# ** or set the PENTAHO_DI_JAVA_OPTIONS environment variable      **
# ******************************************************************

if [ -z "$PENTAHO_DI_JAVA_OPTIONS" ]; then
    PENTAHO_DI_JAVA_OPTIONS="-Xmx6144m -XX:MaxPermSize=1024m"
fi

#OPT="$OPT $PENTAHO_DI_JAVA_OPTIONS -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT"
OPT="-Xmx256m -cp $CLASSPATH $OPT -Djava.library.path=$BASEDIR/$LIBPATH -Dorg.eclipse.swt.browser.DefaultType=mozilla"
# ***************
# ** Run...    **
# ***************
#echo $_PENTAHO_JAVA $OPT org.pentaho.di.ui.spoon.Spoon 

# if there are too many open file error. uncomment following statement, need root privilege;
# ulimit -n 100000

#"$_PENTAHO_JAVA" $OPT $STARTUP -lib $BASEDIR/$LIBPATH "${1+$@}"
"$_PENTAHO_JAVA" $OPT org.pentaho.di.ui.spoon.Spoon "${1+$@}"
