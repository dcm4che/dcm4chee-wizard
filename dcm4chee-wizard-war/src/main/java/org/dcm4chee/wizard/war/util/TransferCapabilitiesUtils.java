package org.dcm4chee.wizard.war.util;

import java.util.EnumSet;
import org.dcm4che.data.UID;

import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.QueryOption;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.TransferCapability.Role;

public class TransferCapabilitiesUtils {

    private static final String[] IMAGE_TSUIDS = { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian,
        UID.DeflatedExplicitVRLittleEndian, UID.ExplicitVRBigEndian, UID.JPEGBaseline1, UID.JPEGExtended24,
        UID.JPEGLossless, UID.JPEGLosslessNonHierarchical14, UID.JPEGLSLossless, UID.JPEGLSLossyNearLossless,
        UID.JPEG2000LosslessOnly, UID.JPEG2000, UID.RLELossless };
    private static final String[] VIDEO_TSUIDS = { UID.JPEGBaseline1, UID.MPEG2, UID.MPEG2MainProfileHighLevel,
        UID.MPEG4AVCH264BDCompatibleHighProfileLevel41, UID.MPEG4AVCH264HighProfileLevel41 };
    private static final String[] OTHER_TSUIDS = { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian,
        UID.DeflatedExplicitVRLittleEndian, UID.ExplicitVRBigEndian, };
    private static final String[] IMAGE_CUIDS = { UID.ComputedRadiographyImageStorage,
        UID.DigitalXRayImageStorageForPresentation, UID.DigitalXRayImageStorageForProcessing,
        UID.DigitalMammographyXRayImageStorageForPresentation, UID.DigitalMammographyXRayImageStorageForProcessing,
        UID.DigitalIntraOralXRayImageStorageForPresentation, UID.DigitalIntraOralXRayImageStorageForProcessing,
        UID.CTImageStorage, UID.EnhancedCTImageStorage, UID.UltrasoundMultiFrameImageStorageRetired,
        UID.UltrasoundMultiFrameImageStorage, UID.MRImageStorage, UID.EnhancedMRImageStorage,
        UID.EnhancedMRColorImageStorage, UID.NuclearMedicineImageStorageRetired, UID.UltrasoundImageStorageRetired,
        UID.UltrasoundImageStorage, UID.EnhancedUSVolumeStorage, UID.SecondaryCaptureImageStorage,
        UID.MultiFrameGrayscaleByteSecondaryCaptureImageStorage,
        UID.MultiFrameGrayscaleWordSecondaryCaptureImageStorage,
        UID.MultiFrameTrueColorSecondaryCaptureImageStorage, UID.XRayAngiographicImageStorage,
        UID.EnhancedXAImageStorage, UID.XRayRadiofluoroscopicImageStorage, UID.EnhancedXRFImageStorage,
        UID.XRayAngiographicBiPlaneImageStorageRetired, UID.XRay3DAngiographicImageStorage,
        UID.XRay3DCraniofacialImageStorage, UID.BreastTomosynthesisImageStorage,
        UID.IntravascularOpticalCoherenceTomographyImageStorageForPresentation,
        UID.IntravascularOpticalCoherenceTomographyImageStorageForProcessing, UID.NuclearMedicineImageStorage,
        UID.VLEndoscopicImageStorage, UID.VLMicroscopicImageStorage, UID.VLSlideCoordinatesMicroscopicImageStorage,
        UID.VLPhotographicImageStorage, UID.OphthalmicPhotography8BitImageStorage,
        UID.OphthalmicPhotography16BitImageStorage, UID.OphthalmicTomographyImageStorage,
        UID.VLWholeSlideMicroscopyImageStorage, UID.PositronEmissionTomographyImageStorage,
        UID.EnhancedPETImageStorage, UID.RTImageStorage, };
    private static final String[] VIDEO_CUIDS = { UID.VideoEndoscopicImageStorage, UID.VideoMicroscopicImageStorage,
        UID.VideoPhotographicImageStorage, };
    private static final String[] OTHER_CUIDS = { UID.MRSpectroscopyStorage,
        UID.MultiFrameSingleBitSecondaryCaptureImageStorage, UID.StandaloneOverlayStorageRetired,
        UID.StandaloneCurveStorageRetired, UID.TwelveLeadECGWaveformStorage, UID.GeneralECGWaveformStorage,
        UID.AmbulatoryECGWaveformStorage, UID.HemodynamicWaveformStorage,
        UID.CardiacElectrophysiologyWaveformStorage, UID.BasicVoiceAudioWaveformStorage,
        UID.GeneralAudioWaveformStorage, UID.ArterialPulseWaveformStorage, UID.RespiratoryWaveformStorage,
        UID.StandaloneModalityLUTStorageRetired, UID.StandaloneVOILUTStorageRetired,
        UID.GrayscaleSoftcopyPresentationStateStorageSOPClass, UID.ColorSoftcopyPresentationStateStorageSOPClass,
        UID.PseudoColorSoftcopyPresentationStateStorageSOPClass,
        UID.BlendingSoftcopyPresentationStateStorageSOPClass, UID.XAXRFGrayscaleSoftcopyPresentationStateStorage,
        UID.RawDataStorage, UID.SpatialRegistrationStorage, UID.SpatialFiducialsStorage,
        UID.DeformableSpatialRegistrationStorage, UID.SegmentationStorage, UID.SurfaceSegmentationStorage,
        UID.RealWorldValueMappingStorage, UID.StereometricRelationshipStorage, UID.LensometryMeasurementsStorage,
        UID.AutorefractionMeasurementsStorage, UID.KeratometryMeasurementsStorage,
        UID.SubjectiveRefractionMeasurementsStorage, UID.VisualAcuityMeasurementsStorage,
        UID.SpectaclePrescriptionReportStorage, UID.OphthalmicAxialMeasurementsStorage,
        UID.IntraocularLensCalculationsStorage, UID.MacularGridThicknessAndVolumeReportStorage,
        UID.OphthalmicVisualFieldStaticPerimetryMeasurementsStorage, UID.BasicStructuredDisplayStorage,
        UID.BasicTextSRStorage, UID.EnhancedSRStorage, UID.ComprehensiveSRStorage, UID.ProcedureLogStorage,
        UID.MammographyCADSRStorage, UID.KeyObjectSelectionDocumentStorage, UID.ChestCADSRStorage,
        UID.XRayRadiationDoseSRStorage, UID.ColonCADSRStorage, UID.ImplantationPlanSRStorage,
        UID.EncapsulatedPDFStorage, UID.EncapsulatedCDAStorage, UID.StandalonePETCurveStorageRetired,
        UID.RTDoseStorage, UID.RTStructureSetStorage, UID.RTBeamsTreatmentRecordStorage, UID.RTPlanStorage,
        UID.RTBrachyTreatmentRecordStorage, UID.RTTreatmentSummaryRecordStorage, UID.RTIonPlanStorage,
        UID.RTIonBeamsTreatmentRecordStorage, UID.StorageCommitmentPushModelSOPClass,
        UID.ModalityPerformedProcedureStepSOPClass, };

    private static final String[] QUERY_CUIDS = { UID.PatientRootQueryRetrieveInformationModelFIND,
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired, UID.ModalityWorklistInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelGET, UID.StudyRootQueryRetrieveInformationModelGET,
        UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired,
        UID.PatientRootQueryRetrieveInformationModelMOVE, UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    private static void addTCs(ApplicationEntity ae, EnumSet<QueryOption> queryOpts, TransferCapability.Role role,
            String[] cuids, String... tss) {
        for (String cuid : cuids)
            addTC(ae, queryOpts, role, cuid, tss);
    }

    private static void addTC(ApplicationEntity ae, EnumSet<QueryOption> queryOpts, TransferCapability.Role role, String cuid,
            String... tss) {
        String name = UID.nameOf(cuid).replace('/', ' ');
        TransferCapability tc = new TransferCapability(name + ' ' + role, cuid, role, tss);
        tc.setQueryOptions(queryOpts);
        ae.addTransferCapability(tc);
    }
    
    public static void addTCsToAE(ApplicationEntity ae) {
        addVerificationStorageTransferCapabilities(ae);
	    addTCs(ae, null, Role.SCP, IMAGE_CUIDS, IMAGE_TSUIDS);
	    addTCs(ae, null, Role.SCP, VIDEO_CUIDS, VIDEO_TSUIDS);
	    addTCs(ae, null, Role.SCP, OTHER_CUIDS, OTHER_TSUIDS);
	    addTCs(ae, null, Role.SCU, IMAGE_CUIDS, IMAGE_TSUIDS);
	    addTCs(ae, null, Role.SCU, VIDEO_CUIDS, VIDEO_TSUIDS);
	    addTCs(ae, null, Role.SCU, OTHER_CUIDS, OTHER_TSUIDS);
	    addTCs(ae, EnumSet.allOf(QueryOption.class), Role.SCP, QUERY_CUIDS, UID.ImplicitVRLittleEndian);
    }
    
    private static void addVerificationStorageTransferCapabilities(ApplicationEntity ae) {
        String cuid = UID.VerificationSOPClass;
        String name = UID.nameOf(cuid).replace('/', ' ');
        ae.addTransferCapability(new TransferCapability(name + " SCP", cuid, TransferCapability.Role.SCP,
        UID.ImplicitVRLittleEndian));
        ae.addTransferCapability(new TransferCapability(name + " SCU", cuid, TransferCapability.Role.SCU,
        UID.ImplicitVRLittleEndian));
    }
}
