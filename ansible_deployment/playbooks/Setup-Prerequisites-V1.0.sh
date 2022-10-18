#!/bin/bash
###############################################################################
#####  Utility      :: Pre-requiresites.sh                                #####
#####  Description  :: this utility will install required packages     #####
#####  Team         :: InfraDevOps                                        #####
###############################################################################

RED=`tput setaf 1`
GREEN=`tput setaf 2`
RESET=`tput sgr0`
YELLOW=`tput setaf 3`
CYAN=`tput setaf 6`
BLUE=`tput setaf 4`
MAGENTA=`tput setaf 5`
BLANK=`echo`

OS_VERSION=$(cat /etc/os-release | grep -w VERSION_ID | awk -F "=" '{print $2}' | tr -d \")
OS_MAJOR_VERSION=$(echo -e $OS_VERSION | cut -f1 -d '.')
OS_NAME=$(cat /etc/os-release  | grep -w NAME | awk -F "=" '{print $2}' | tr -d \" | awk '{print $1}' | tr '[:upper:]' '[:lower:]')

DOCK_CE_URL="https://download.docker.com/linux/centos/7/x86_64/stable/Packages/docker-ce-18.03.0.ce-1.el7.centos.x86_64.rpm"
DOCK_CE_SEL_URL="http://mirror.centos.org/centos/7/extras/x86_64/Packages/container-selinux-2.107-1.el7_6.noarch.rpm"
EPEL_RPM_URL="https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm"

########################################
##### function to operating system #####
########################################
function check_operating_system()
{
echo -e "${BLUE}[\xE2\x9C\x94] Checking the Operating system & Version and installing required tools ${RESET}"
   if [ -z "$OS_NAME" ]; then
     echo -e "${RED}[\xE2\x9D\x8C] Failed to get the present system operating system, exiting...${RESET}"
     exit 1
   fi
   case $OS_NAME in
     'red')
        OPERATING_SYSTEM='RedHat'
        if [ $OS_MAJOR_VERSION -lt 7 ]; then
             echo -e "${RED}[\xE2\x9D\x8C] your operating system version is $OPERATING_SYSTEM $OS_VERSION but presently supports 12.x only, exiting...${RESET}"
             exit 1
        fi
        install_yum_utils_package                  #collection of tools and programs for managing yum repositories
        install_wget_package_redhat                #wget for downloads any package
        install_rpm_package_redhat                 #rpm for properties.rpm file extract
        enable_epel_repository                     #epel package
        install_policycoreutils_python_package     #policycoreutils for pythonpackage
        install_python3_package                    #python3 package
        install_pip_package_redhat                 #pip package
        install_container_selinux_package          #container-selinux for docker
        install_docker_daemon_centos               #docker package
        start_docker_daemon                        #start docker
        enable_docker_daemon                       #enable docker
            install_docker_compose_package             #docker-compose package
        install_j2_package                         #j2 package for convert .j2 templates to compose
        ;;
     'centos')
        OPERATING_SYSTEM='Centos'
        if [ $OS_MAJOR_VERSION -lt 7 ]; then
             echo -e "${RED}[\xE2\x9D\x8C] your operating system version is $OPERATING_SYSTEM $OS_VERSION but presently supports 12.x only, exiting...${RESET}"
             exit 1
        fi
        install_yum_utils_package
        install_wget_package_redhat
        install_git
        install_rpm_package_redhat
        enable_epel_repository
        install_policycoreutils_python_package
        install_python3_package
        install_pip_package_redhat
        install_container_selinux_package
        install_docker_daemon_centos
        start_docker_daemon
        enable_docker_daemon
                install_docker_compose_package
        install_j2_package
        ;;
     'ubuntu')
        OPERATING_SYSTEM='Ubuntu'
        if [ $OS_MAJOR_VERSION -lt 12 ]; then
             echo -e "${RED}[\xE2\x9D\x8C] your operating system version is $OPERATING_SYSTEM $OS_VERSION but presently supports 12.x only, exiting...${RESET}"
             exit 1
        fi
        install_wget_package_ubuntu
                install_git_ubuntu
        install_rpm_package_ubuntu
        install_pip_package_ubuntu
        install_python3_package_ubuntu
        install_docker_ubuntu
        enable_docker_daemon
                install_docker_compose_package_ubuntu
        install_j2_package
        ;;
     *)
        echo -e "${RED}[\xE2\x9D\x8C] your operating system is $OS_NAME but presently supports: RedHat|Centos|Ubuntu operating system, exiting...${RESET}"
        exit 1
   esac

   echo -e "${BLUE}[\xE2\x9C\x94] setup tools are installed on $OPERATING_SYSTEM $OS_VERSION${RESET}"
}

#################################################
##### function to install yum-utils package    #####
#################################################
function install_yum_utils_package()
{
  YUM_UTILS_PACKAGE_CHECK=$(rpm -qa yum-utils)
  if [ -z "${YUM_UTILS_PACKAGE_CHECK}" ]; then
      yum install yum-utils -y > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] yum-utils package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install yum-utils package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] yum-utils package is already installed....${RESET}"
  fi
}
############################################################
##### function to install wget packet on RedHat|Centos #####
############################################################
function install_wget_package_redhat()
{
  WGET_PACKAGE_CHECK=$(rpm -qa wget)
[O  if [ -z "${WGET_PACKAGE_CHECK}" ]; then
      yum install wget -y > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] wget package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install wget package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] wget package is already installed....${RESET}"
  fi
}
#####################################################
##### function to install wget packet on Ubuntu #####
#####################################################
function install_wget_package_ubuntu()
{
  WGET_PACKAGE_CHECK=$( dpkg -l  | grep -i wget)
  if [ -z "${WGET_PACKAGE_CHECK}" ]; then
  sudo apt-get install wget -y > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] wget package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install wget package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] wget package is already installed....${RESET}"
  fi
}

############################################################
##### function to install wget packet on RedHat|Centos #####
############################################################
function install_git()
{
  GIT_PACKAGE_CHECK=$(rpm -qa git)
  if [ -z "${GiT_PACKAGE_CHECK}" ]; then
      yum install git -y > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] git package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install git package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] git package is already installed....${RESET}"
  fi
}

############################################################
##### function to install wget packet on RedHat|Centos #####
############################################################
function install_git_ubuntu()
{
  GIT_PACKAGE_CHECK=$(rpm -qa git)
  if [ -z "${GiT_PACKAGE_CHECK}" ]; then
      sudo apt install git -y > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] git package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install git package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] git package is already installed....${RESET}"
  fi
}

############################################################
##### function to install rpm packet on RedHat|Centos #####
############################################################
function install_rpm_package_redhat()
{
  WGET_PACKAGE_CHECK=$(rpm -qa rpm)
  if [ -z "${WGET_PACKAGE_CHECK}" ]; then
      yum install rpm -y > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] rpm package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install rpm package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] rpm package is already installed....${RESET}"
  fi
}
#################################################
##### Function to check the epel repository #####
#################################################
function enable_epel_repository()
{
  EPEL_PACKAGE_CHECK=$(rpm -qa epel-release)
  if [ -z "${EPEL_PACKAGE_CHECK}" ]; then
     rpm -Uvh ${EPEL_RPM_URL} > /dev/null 2>&1
     if [ $? -eq 0 ]; then
       echo -e "${GREEN}[\xE2\x9C\x94] epel-release package is installed....${RESET}"
     else
       echo -e "${RED}[\xE2\x9D\x8C] failed to install epel-release package, exiting...${RESET}"
       exit 1
     fi
  else
     echo -e "${CYAN}[\xE2\x9C\x94] epel-release repository is already enabled...${RESET}"
  fi
}
#####################################################
##### function to install rpm packet on Ubuntu #####
#####################################################
function install_rpm_package_ubuntu()
{
  WGET_PACKAGE_CHECK=$( dpkg -l  | grep -i rpm)
  if [ -z "${WGET_PACKAGE_CHECK}" ]; then
  sudo apt-get install rpm -y > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] rpm package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install rpm package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] rpm package is already installed....${RESET}"
  fi
}
#########################################################
##### function to install container selinux package #####
#########################################################
function install_container_selinux_package()
{
  CE_PACKAGE_CHECK=$(rpm -qa container-selinux)
  if [ -z "${CE_PACKAGE_CHECK}" ]; then
      sudo yum install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
      if [ $? -eq 0 ]; then
          echo -e "${CYAN}[\xE2\x9C\x94] container-selinux package is already installed....${RESET}"
      else
           echo -e "${RED}[\xE2\x9D\x8C] failed to install container-selinux package, exiting....${RESET}"
      fi
  fi
}

##############################################################
##### function to install docker dependency on RedHat    #####
##############################################################
function install_docker_dep_redhat()
{
  DOCKER_PACKAGE_CHECK=$(rpm -qa lib64ltdl7)
  if [ -z "${DOCKER_PACKAGE_CHECK}" ]; then
      rpm -Uvh ${LIB_LTDL} > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] lib64ltdl7 package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install lib64ltdl7 package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] lib64ltdl7 package is already installed....${RESET}"
  fi
}

##############################################################
##### function to install docker daemon on RedHat        #####
##############################################################
function install_docker_daemon_redhat()
{
  DOCKER_PACKAGE_CHECK=$(rpm -qa docker-ce)
  if [ -z "${DOCKER_PACKAGE_CHECK}" ]; then
      rpm -Uvh ${DOCK_CE_URL} > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] docker-ce package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install docker-ce package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] docker-ce package is already installed....${RESET}"
  fi
}
##############################################################
##### function to install docker daemon on Centos        #####
##############################################################
function install_docker_daemon_centos()
{
  DOCKER_PACKAGE_CHECK=$(rpm -qa docker-ce)
  if [ -z "${DOCKER_PACKAGE_CHECK}" ]; then
      yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] docker-ce-repo is added....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to add repo docker-ce, exiting....${RESET}"
          exit 1
      fi
      yum install -y docker-ce  > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] docker-ce package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install docker-ce package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] docker-ce package is already installed....${RESET}"
  fi
}
################################################
##### function to install docker on Ubuntu #####
################################################
function install_docker_ubuntu()
{
  DOCKER_PACKAGE_CHECK=$( dpkg -l |grep docker.io)
  if [ -z "${DOCKER_PACKAGE_CHECK}" ]; then
       sudo apt install docker.io -y > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] docker-ce package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install docker-ce package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] docker-ce package is already installed....${RESET}"
  fi
}

###########################################
##### function to start docker daemon #####
###########################################
function start_docker_daemon()
{
  DOCKER_PROCESS_CHECK=$(pgrep -x dockerd)
  if [ -z "${DOCKER_PROCESS_CHECK}" ]; then
      systemctl start docker > /dev/null 2>&1
      if [ $? -eq  0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] docker daemon is started....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to start docker daemon, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] docker daemon is already running.....${RESET}"
  fi
}

############################################
##### function to enable docker daemon #####
############################################
function enable_docker_daemon()
{
  DOCKER_PROCCES_ENABLE_CHECK="/etc/systemd/system/multi-user.target.wants/docker.service"
  if [ ! -f "${DOCKER_PROCCES_ENABLE_CHECK}" ]; then
      systemctl enable docker > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] docker daemon is enabled to start at boot time....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed tp enable docker daemon, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] docker daemon is already enabled to start at boot time....${RESET}"
  fi
}
##############################################################
##### function to install policycoreutils python modules #####
##############################################################
function install_policycoreutils_python_package()
{
  PYTHON_CORE_UTILS_PACKAGE_CHECK=$(rpm -qa policycoreutils-python)
  if [ -z "${PYTHON_CORE_UTILS_PACKAGE_CHECK}" ]; then
      yum install policycoreutils-python3 -y > /dev/null 2>&1
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] policycoreutils-python package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install policycoreutils-python package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] policycoreutils-python package is already installed....${RESET}"
  fi
}
#######################################################################
##### Function to install the python pip package on RedHat|Centos #####
#######################################################################
function install_pip_package_redhat()
{
  PIP_PACKAGE_CHECK=$(rpm -qa python-pip)
  if [ -z "${PIP_PACKAGE_CHECK}" ]; then
    yum install python3-pip -y > /dev/null 2>&1
    if [ $? -eq 0 ]; then
      echo -e "${GREEN}[\xE2\x9C\x94] python package pip package is installed....${RESET}"
    else
      echo -e "${RED}[\xE2\x9D\x8C] failed to install python package pip, exiting...${RESET}"
      exit 1
    fi
  else
    echo -e "${CYAN}[\xE2\x9C\x94] python2-pip package is already installed....${RESET}"
    pip3 install --upgrade pip > /dev/null 2>&1
    if [ $? -ne 0 ]; then
      echo -e "${RED}[\xE2\x9D\x8C] failed to upgrade pip package, exiting...${RESET}"
      exit 1
    fi
  fi
}
################################################################
##### Function to install the python pip package on Ubuntu #####
################################################################
function install_pip_package_ubuntu()
{
  PIP_PACKAGE_CHECK=$( dpkg -l  | grep -i python3-pip)
  if [ -z "${PIP_PACKAGE_CHECK}" ]; then
  sudo apt-get install -y python3-pip  > /dev/null 2>&1
    if [ $? -eq 0 ]; then
      echo -e "${GREEN}[\xE2\x9C\x94] python package pip package is installed....${RESET}"
    else
      echo -e "${RED}[\xE2\x9D\x8C] failed to install python package pip, exiting...${RESET}"
      exit 1
    fi
  else
    echo -e "${CYAN}[\xE2\x9C\x94] python2-pip package is already installed, checking for upgrades....${RESET}"
  sudo pip3 install --upgrade pip    > /dev/null 2>&1
  sudo pip install --upgrade virtualenv   > /dev/null 2>&1
    if [ $? -ne 0 ]; then
      echo -e "${RED}[\xE2\x9D\x8C] failed to upgrade pip package, exiting...${RESET}"
      exit 1
    fi
  fi
}

##############################################################
##### function to install docker-compose                 #####
##############################################################
function install_docker_compose_package()
{
  docker_compose_PACKAGE_CHECK=$(rpm -qa docker-compose)
  if [ -z "${docker_compose_PACKAGE_CHECK}" ]; then
      sudo curl -L "https://github.com/docker/compose/releases/download/1.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose > /dev/null 2>&1
      sudo chmod +x /usr/local/bin/docker-compose


      if [ $? -eq 0 ]; then
          sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
		  sudo ln -s /bin/python3 /bin/python
          echo -e "${GREEN}[\xE2\x9C\x94] docker_compose package is installed....${RESET}"

      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install docker_compose package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] docker_compose package is already installed....${RESET}"
  fi
}

##############################################################
##### function to install docker-compose                 #####
##############################################################
function install_docker_compose_package_ubuntu()
{
  docker_compose_PACKAGE_CHECK=$(rpm -qa docker-compose)
  if [ -z "${docker_compose_PACKAGE_CHECK}" ]; then
      apt install docker-compose -y > /dev/null 2>&1

      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] docker_compose package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install docker_compose package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] docker_compose package is already installed....${RESET}"
  fi
}

##############################################################
##### function to install j2                             #####
##############################################################
function install_j2_package()
{
  j2_PACKAGE_CHECK=$(rpm -qa j2)
  if [ -z "${j2_PACKAGE_CHECK}" ]; then
      pip3 install j2cli > /dev/null 2>&1

      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] j2 package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install j2 package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] j2 package is already installed....${RESET}"
  fi
}

##############################################################
##### function to install python3                        #####
##############################################################
function install_python3_package()
{
  python3_PACKAGE_CHECK=$(rpm -qa python3)
  if [ -z "${python3_PACKAGE_CHECK}" ]; then
      yum install python3 -y
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] python3 package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install python3 package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] python3 package is already installed....${RESET}"
  fi
}

##############################################################
##### function to install python3                        #####
##############################################################
function install_python3_package_ubuntu()
{
  python3_PACKAGE_CHECK=$(rpm -qa python3)
  if [ -z "${python3_PACKAGE_CHECK}" ]; then
    apt install python3 -y
      if [ $? -eq 0 ]; then
          echo -e "${GREEN}[\xE2\x9C\x94] python3 package is installed....${RESET}"
      else
          echo -e "${RED}[\xE2\x9D\x8C] failed to install python3 package, exiting....${RESET}"
          exit 1
      fi
  else
      echo -e "${CYAN}[\xE2\x9C\x94] python3 package is already installed....${RESET}"
  fi
}


###############################################
##### Function to open a port in firewall #####
###############################################
function firewall_open_port()
{
   firewall-cmd --add-port=8080/tcp --add-port=80/tcp --add-port=443/tcp --add-port=27017/tcp --add-port=2181/tcp --add-port=9092/tcp --permanent && firewall-cmd --reload
   echo -e "${GREEN}[\xE2\x9C\x94] $PORT opened in firewall ${RESET}"
}

###############################################
##### Function for registry               #####
###############################################
function registry()
{
   path=/etc/systemd/system/docker.service.d/docker.conf
   if [ -z "${path}" ]; then
    sed -i "s/--insecure-registry appx.tools.publicis.sapient.com/--insecure-registry setup-speedy.tools.publicis.sapient.com/g" ${path}
        systemctl daemon-reload
        systemctl restart docker

        else
          mkdir -p /etc/systemd/system/docker.service.d/
          touch docker.conf
          echo "[Service]" >>${path}
      echo "ExecStart=" >>${path}
      echo "ExecStart=/usr/bin/dockerd --insecure-registry setup-speedy.tools.publicis.sapient.com" >>${path}
          echo "path added"
          sed '3,$d' /etc/systemd/system/docker.service.d/docker.conf
      systemctl daemon-reload
          systemctl restart docker
   fi
   echo -e "${GREEN}[\xE2\x9C\x94] added registry path ${RESET}"
}

check_operating_system
firewall_open_port
registry

