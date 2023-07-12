cmake_minimum_required(VERSION 3.1.3)

project(sndfile VERSION 1.0.29)

#
# Variables
#

set(CMAKE_C_STANDARD 99)
set(CMAKE_C_STANDARD_REQUIRED TRUE)

set(PACKAGE_NAME lib${PROJECT_NAME})
set(CPACK_PACKAGE_VERSION_MAJOR ${PROJECT_VERSION_MAJOR})
set(CPACK_PACKAGE_VERSION_MINOR ${PROJECT_VERSION_MINOR})
set(CPACK_PACKAGE_VERSION_PATCH ${PROJECT_VERSION_PATCH})
set(CPACK_PACKAGE_VERSION_STAGE "pre1")
set(CPACK_PACKAGE_VERSION_FULL "${PROJECT_VERSION}${CPACK_PACKAGE_VERSION_STAGE}")

#
# System-wide includes
#

include(GNUInstallDirs)
include(FeatureSummary)
include(CMakeDependentOption)

#
# Options
#

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

#
# Setup definitions
#

include(SndFileChecks)

set(HAVE_EXTERNAL_XIPH_LIBS ${ENABLE_EXTERNAL_LIBS})
set(HAVE_ALSA_ASOUNDLIB_H ${ALSA_FOUND})
set(HAVE_SNDIO_H ${SNDIO_FOUND})

set(ENABLE_EXPERIMENTAL_CODE ${ENABLE_EXPERIMENTAL})
set(HAVE_SPEEX ${ENABLE_EXPERIMENTAL})
set(HAVE_OPUS ${ENABLE_EXPERIMENTAL})

set_package_properties(Ogg PROPERTIES
        TYPE RECOMMENDED
        URL "www.xiph.org/ogg/"
        DESCRIPTION "library for manipulating ogg bitstreams"
        PURPOSE "Required to enable Vorbis, Speex, and Opus support"
        )
set_package_properties(VorbisEnc PROPERTIES
        TYPE RECOMMENDED
        URL "www.vorbis.com/"
        DESCRIPTION "open source lossy audio codec"
        PURPOSE "Enables Vorbis support"
        )
set_package_properties(FLAC PROPERTIES
        TYPE RECOMMENDED
        URL "www.xiph.org/flac/"
        DESCRIPTION "Free Lossless Audio Codec Library"
        PURPOSE "Enables FLAC support"
        )
set_package_properties(Opus PROPERTIES
        TYPE RECOMMENDED
        URL "www.opus-codec.org/"
        DESCRIPTION "Standardized open source low-latency fullband codec"
        PURPOSE "Enables experimental Opus support"
        )
set_package_properties(Speex PROPERTIES TYPE OPTIONAL
        URL "www.speex.org/"
        DESCRIPTION "an audio codec tuned for speech"
        PURPOSE "Enables experemental Speex support"
        )
set_package_properties(SQLite3 PROPERTIES
        TYPE OPTIONAL
        URL "www.sqlite.org/"
        DESCRIPTION "light weight SQL database engine."
        PURPOSE "Enables regtest"
        )

feature_summary(WHAT ALL)

#
# Setup configuration
#

configure_file(src/config.h.cmake src/config.h)

configure_file(src/sndfile.h.in src/sndfile.h)

set(prefix ${CMAKE_INSTALL_PREFIX})
set(exec_prefix "\\$\\{prefix\\}")
set(libdir "\\$\\{prefix\\}/${CMAKE_INSTALL_LIBDIR}")
set(includedir "\\$\\{prefix\\}/${CMAKE_INSTALL_INCLUDEDIR}")
set(VERSION ${PROJECT_VERSION})

configure_file(sndfile.pc.in sndfile.pc @ONLY)

#
# libsndfile
#

# Public libsndfile headers
set(sndfile_HDRS
        src/sndfile.hh
        ${CMAKE_CURRENT_BINARY_DIR}/src/sndfile.h
        )

#
# libsndfile static library
#

add_library(sndfile
        SHARED
        src/sfconfig.h
        src/sfendian.h
        src/sf_unistd.h
        src/common.h
        src/common.c
        src/file_io.c
        src/command.c
        src/pcm.c
        src/ulaw.c
        src/alaw.c
        src/float32.c
        src/double64.c
        src/ima_adpcm.c
        src/ms_adpcm.c
        src/gsm610.c
        src/dwvw.c
        src/vox_adpcm.c
        src/interleave.c
        src/strings.c
        src/dither.c
        src/cart.c
        src/broadcast.c
        src/audio_detect.c
        src/ima_oki_adpcm.c
        src/ima_oki_adpcm.h
        src/alac.c
        src/chunk.c
        src/ogg.h
        src/ogg.c
        src/chanmap.h
        src/chanmap.c
        src/id3.c
        $<$<BOOL:${WIN32}>:src/windows.c>
        $<$<AND:$<BOOL:${WIN32}>,$<BOOL:${WIN32}>>:src/version-metadata.rc>
        src/sndfile.c
        src/aiff.c
        src/au.c
        src/avr.c
        src/caf.c
        src/dwd.c
        src/flac.c
        src/g72x.c
        src/htk.c
        src/ircam.c
        src/macos.c
        src/mat4.c
        src/mat5.c
        src/nist.c
        src/paf.c
        src/pvf.c
        src/raw.c
        src/rx2.c
        src/sd2.c
        src/sds.c
        src/svx.c
        src/txw.c
        src/voc.c
        src/wve.c
        src/w64.c
        src/wavlike.h
        src/wavlike.c
        src/wav.c
        src/xi.c
        src/mpc2k.c
        src/rf64.c
        src/ogg_vorbis.c
        src/ogg_speex.c
        src/ogg_pcm.c
        src/ogg_opus.c
        src/ogg_vcomment.c
        src/nms_adpcm.c
        src/GSM610/config.h
        src/GSM610/gsm.h
        src/GSM610/gsm610_priv.h
        src/GSM610/add.c
        src/GSM610/code.c
        src/GSM610/decode.c
        src/GSM610/gsm_create.c
        src/GSM610/gsm_decode.c
        src/GSM610/gsm_destroy.c
        src/GSM610/gsm_encode.c
        src/GSM610/gsm_option.c
        src/GSM610/long_term.c
        src/GSM610/lpc.c
        src/GSM610/preprocess.c
        src/GSM610/rpe.c
        src/GSM610/short_term.c
        src/GSM610/table.c
        src/G72x/g72x.h
        src/G72x/g72x_priv.h
        src/G72x/g721.c
        src/G72x/g723_16.c
        src/G72x/g723_24.c
        src/G72x/g723_40.c
        src/G72x/g72x.c
        src/ALAC/ALACAudioTypes.h
        src/ALAC/ALACBitUtilities.h
        src/ALAC/EndianPortable.h
        src/ALAC/aglib.h
        src/ALAC/dplib.h
        src/ALAC/matrixlib.h
        src/ALAC/alac_codec.h
        src/ALAC/shift.h
        src/ALAC/ALACBitUtilities.c
        src/ALAC/ag_dec.c
        src/ALAC/ag_enc.c
        src/ALAC/dp_dec.c
        src/ALAC/dp_enc.c
        src/ALAC/matrix_dec.c
        src/ALAC/matrix_enc.c
        src/ALAC/alac_decoder.c
        src/ALAC/alac_encoder.c
        ${sndfile_HDRS}
        ${CMAKE_CURRENT_BINARY_DIR}/src/config.h
        Wrapper.cpp
        )

target_include_directories(sndfile
        PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}/src>
        $<INSTALL_INTERFACE:${CMAKE_INSTALL_INCLUDEDIR}>
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/src>
        )
target_link_libraries(sndfile
        PRIVATE
        $<$<BOOL:${LIBM_REQUIRED}>:m>
        $<$<BOOL:${HAVE_EXTERNAL_XIPH_LIBS}>:Vorbis::VorbisEnc>
        $<$<BOOL:${HAVE_EXTERNAL_XIPH_LIBS}>:FLAC::FLAC>
        $<$<AND:$<BOOL:${ENABLE_EXPERIMENTAL}>,$<BOOL:${HAVE_EXTERNAL_XIPH_LIBS}>,$<BOOL:${HAVE_SPEEX}>>:Speex::Speex>
        $<$<BOOL:${HAVE_EXTERNAL_XIPH_LIBS}>:Opus::Opus>
        )
set_target_properties(sndfile PROPERTIES
        PUBLIC_HEADER "${sndfile_HDRS}"
        )

#
# Installation
#

install(TARGETS sndfile ${sdnfile_PROGRAMS}
        RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
        ARCHIVE DESTINATION ${CMAKE_INSTALL_LIBDIR}
        LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
        PUBLIC_HEADER DESTINATION ${CMAKE_INSTALL_INCLUDEDIR})


install(FILES ${html_DATA} DESTINATION ${CMAKE_INSTALL_DOCDIR})

install(FILES ${CMAKE_CURRENT_BINARY_DIR}/sndfile.pc DESTINATION ${CMAKE_INSTALL_LIBDIR}/pkgconfig)
