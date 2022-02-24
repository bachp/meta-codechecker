SUMMARY = "CodeChecker static analysis tool"
HOMEPAGE = "https://codechecker.readthedocs.io/en/latest/"
LICENSE = "Apache-2.0"

# The pypi package doesn't include a license file
LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=2e982d844baa4df1c80de75470e0c5cb"

SRC_URI[sha256sum] = "c793a1951f58baf0ba414e41f6458af6292f6428c699e84b8b71da161d834f0b"

PYPI_PACKAGE = "codechecker"

inherit pypi distutils3-base

DEPENDS += "\
    ${PYTHON_PN}-pip-native \
    python3-lxml python3-sqlalchemy python3-alembic python3-portalocker python3-psutil python3-mypy-extensions python3-thrift \
    python3-pyyaml python3-git python3-gitdb python3-markupsafe python3-smmap \
"

RDEPENDS_${PN} += "python3 python3-modules"

# Requirements from web/requirements.txt
RDEPENDS_${PN} += "python3-lxml python3-sqlalchemy python3-alembic python3-portalocker python3-psutil python3-mypy-extensions python3-thrift"

# Requirements from analyzers/requirements.txt
RDEPENDS_${PN} += "python3-pyyaml python3-git python3-gitdb python3-markupsafe python3-smmap"

RDEPENDS_${PN}_class-native += " clang-native"
RDEPENDS_${PN}_class-nativesdk += " nativesdk-clang"

do_patch_append () {
    bb.build.exec_func('do_relax_versions', d)
}

do_relax_versions() {
    sed -i -e "s/==/>=/g" ${S}/analyzer/requirements.txt
    sed -i -e "s/==/>=/g" ${S}/web/requirements.txt
    sed -i -e "s/==/>=/g" ${S}/build_dist/CodeChecker/lib/python3/codechecker.egg-info/requires.txt
}

do_install() {
    # CodeChecker use a native namespace package and can't be installed using
    # setup.py install, so we can't simply ihnerit from setuptools3
    # Instead, we must install using pip install .
    # This code is inspired by the distutils3.bbclass
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}


    PYTHONPATH=${D}${PYTHON_SITEPACKAGES_DIR} \
    ${STAGING_BINDIR_NATIVE}/${PYTHON_PN}-native/${PYTHON_PN} -m pip install . \
      --no-index --no-deps \
      --prefix=${prefix} \
      --root=${D}

    for i in ${D}${bindir}/* ${D}${sbindir}/*; do
        if [ -f "$i" ]; then
            sed -i -e s:${PYTHON}:${USRBINPATH}/env\ python3:g $i
            sed -i -e s:${STAGING_BINDIR_NATIVE}:${bindir}:g $i
        fi
    done
}

BBCLASSEXTEND += "native nativesdk"
