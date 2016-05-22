
package sd.tp1.clt.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the sd.tp1.clt.ws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DeletePicture_QNAME = new QName("http://SOAP.svr.tp1.sd/", "deletePicture");
    private final static QName _CreateAlbumResponse_QNAME = new QName("http://SOAP.svr.tp1.sd/", "createAlbumResponse");
    private final static QName _SendMetadata_QNAME = new QName("http://SOAP.svr.tp1.sd/", "sendMetadata");
    private final static QName _DeleteAlbumResponse_QNAME = new QName("http://SOAP.svr.tp1.sd/", "deleteAlbumResponse");
    private final static QName _GetPictureData_QNAME = new QName("http://SOAP.svr.tp1.sd/", "getPictureData");
    private final static QName _GetPictureDataResponse_QNAME = new QName("http://SOAP.svr.tp1.sd/", "getPictureDataResponse");
    private final static QName _SendMetadataResponse_QNAME = new QName("http://SOAP.svr.tp1.sd/", "sendMetadataResponse");
    private final static QName _GetListOfAlbumsResponse_QNAME = new QName("http://SOAP.svr.tp1.sd/", "getListOfAlbumsResponse");
    private final static QName _UploadPicture_QNAME = new QName("http://SOAP.svr.tp1.sd/", "uploadPicture");
    private final static QName _DeletePictureResponse_QNAME = new QName("http://SOAP.svr.tp1.sd/", "deletePictureResponse");
    private final static QName _GetListOfPictures_QNAME = new QName("http://SOAP.svr.tp1.sd/", "getListOfPictures");
    private final static QName _GetListOfPicturesResponse_QNAME = new QName("http://SOAP.svr.tp1.sd/", "getListOfPicturesResponse");
    private final static QName _CreateAlbum_QNAME = new QName("http://SOAP.svr.tp1.sd/", "createAlbum");
    private final static QName _GetListOfAlbums_QNAME = new QName("http://SOAP.svr.tp1.sd/", "getListOfAlbums");
    private final static QName _UploadPictureResponse_QNAME = new QName("http://SOAP.svr.tp1.sd/", "uploadPictureResponse");
    private final static QName _DeleteAlbum_QNAME = new QName("http://SOAP.svr.tp1.sd/", "deleteAlbum");
    private final static QName _GetPictureDataResponseReturn_QNAME = new QName("", "return");
    private final static QName _UploadPictureArg2_QNAME = new QName("", "arg2");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: sd.tp1.clt.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeleteAlbumResponse }
     * 
     */
    public DeleteAlbumResponse createDeleteAlbumResponse() {
        return new DeleteAlbumResponse();
    }

    /**
     * Create an instance of {@link GetPictureData }
     * 
     */
    public GetPictureData createGetPictureData() {
        return new GetPictureData();
    }

    /**
     * Create an instance of {@link GetPictureDataResponse }
     * 
     */
    public GetPictureDataResponse createGetPictureDataResponse() {
        return new GetPictureDataResponse();
    }

    /**
     * Create an instance of {@link SendMetadataResponse }
     * 
     */
    public SendMetadataResponse createSendMetadataResponse() {
        return new SendMetadataResponse();
    }

    /**
     * Create an instance of {@link GetListOfAlbumsResponse }
     * 
     */
    public GetListOfAlbumsResponse createGetListOfAlbumsResponse() {
        return new GetListOfAlbumsResponse();
    }

    /**
     * Create an instance of {@link UploadPicture }
     * 
     */
    public UploadPicture createUploadPicture() {
        return new UploadPicture();
    }

    /**
     * Create an instance of {@link DeletePicture }
     * 
     */
    public DeletePicture createDeletePicture() {
        return new DeletePicture();
    }

    /**
     * Create an instance of {@link CreateAlbumResponse }
     * 
     */
    public CreateAlbumResponse createCreateAlbumResponse() {
        return new CreateAlbumResponse();
    }

    /**
     * Create an instance of {@link SendMetadata }
     * 
     */
    public SendMetadata createSendMetadata() {
        return new SendMetadata();
    }

    /**
     * Create an instance of {@link UploadPictureResponse }
     * 
     */
    public UploadPictureResponse createUploadPictureResponse() {
        return new UploadPictureResponse();
    }

    /**
     * Create an instance of {@link DeleteAlbum }
     * 
     */
    public DeleteAlbum createDeleteAlbum() {
        return new DeleteAlbum();
    }

    /**
     * Create an instance of {@link DeletePictureResponse }
     * 
     */
    public DeletePictureResponse createDeletePictureResponse() {
        return new DeletePictureResponse();
    }

    /**
     * Create an instance of {@link GetListOfPictures }
     * 
     */
    public GetListOfPictures createGetListOfPictures() {
        return new GetListOfPictures();
    }

    /**
     * Create an instance of {@link GetListOfPicturesResponse }
     * 
     */
    public GetListOfPicturesResponse createGetListOfPicturesResponse() {
        return new GetListOfPicturesResponse();
    }

    /**
     * Create an instance of {@link CreateAlbum }
     * 
     */
    public CreateAlbum createCreateAlbum() {
        return new CreateAlbum();
    }

    /**
     * Create an instance of {@link GetListOfAlbums }
     * 
     */
    public GetListOfAlbums createGetListOfAlbums() {
        return new GetListOfAlbums();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeletePicture }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "deletePicture")
    public JAXBElement<DeletePicture> createDeletePicture(DeletePicture value) {
        return new JAXBElement<DeletePicture>(_DeletePicture_QNAME, DeletePicture.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateAlbumResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "createAlbumResponse")
    public JAXBElement<CreateAlbumResponse> createCreateAlbumResponse(CreateAlbumResponse value) {
        return new JAXBElement<CreateAlbumResponse>(_CreateAlbumResponse_QNAME, CreateAlbumResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendMetadata }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "sendMetadata")
    public JAXBElement<SendMetadata> createSendMetadata(SendMetadata value) {
        return new JAXBElement<SendMetadata>(_SendMetadata_QNAME, SendMetadata.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteAlbumResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "deleteAlbumResponse")
    public JAXBElement<DeleteAlbumResponse> createDeleteAlbumResponse(DeleteAlbumResponse value) {
        return new JAXBElement<DeleteAlbumResponse>(_DeleteAlbumResponse_QNAME, DeleteAlbumResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPictureData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "getPictureData")
    public JAXBElement<GetPictureData> createGetPictureData(GetPictureData value) {
        return new JAXBElement<GetPictureData>(_GetPictureData_QNAME, GetPictureData.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPictureDataResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "getPictureDataResponse")
    public JAXBElement<GetPictureDataResponse> createGetPictureDataResponse(GetPictureDataResponse value) {
        return new JAXBElement<GetPictureDataResponse>(_GetPictureDataResponse_QNAME, GetPictureDataResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendMetadataResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "sendMetadataResponse")
    public JAXBElement<SendMetadataResponse> createSendMetadataResponse(SendMetadataResponse value) {
        return new JAXBElement<SendMetadataResponse>(_SendMetadataResponse_QNAME, SendMetadataResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetListOfAlbumsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "getListOfAlbumsResponse")
    public JAXBElement<GetListOfAlbumsResponse> createGetListOfAlbumsResponse(GetListOfAlbumsResponse value) {
        return new JAXBElement<GetListOfAlbumsResponse>(_GetListOfAlbumsResponse_QNAME, GetListOfAlbumsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadPicture }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "uploadPicture")
    public JAXBElement<UploadPicture> createUploadPicture(UploadPicture value) {
        return new JAXBElement<UploadPicture>(_UploadPicture_QNAME, UploadPicture.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeletePictureResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "deletePictureResponse")
    public JAXBElement<DeletePictureResponse> createDeletePictureResponse(DeletePictureResponse value) {
        return new JAXBElement<DeletePictureResponse>(_DeletePictureResponse_QNAME, DeletePictureResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetListOfPictures }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "getListOfPictures")
    public JAXBElement<GetListOfPictures> createGetListOfPictures(GetListOfPictures value) {
        return new JAXBElement<GetListOfPictures>(_GetListOfPictures_QNAME, GetListOfPictures.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetListOfPicturesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "getListOfPicturesResponse")
    public JAXBElement<GetListOfPicturesResponse> createGetListOfPicturesResponse(GetListOfPicturesResponse value) {
        return new JAXBElement<GetListOfPicturesResponse>(_GetListOfPicturesResponse_QNAME, GetListOfPicturesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateAlbum }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "createAlbum")
    public JAXBElement<CreateAlbum> createCreateAlbum(CreateAlbum value) {
        return new JAXBElement<CreateAlbum>(_CreateAlbum_QNAME, CreateAlbum.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetListOfAlbums }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "getListOfAlbums")
    public JAXBElement<GetListOfAlbums> createGetListOfAlbums(GetListOfAlbums value) {
        return new JAXBElement<GetListOfAlbums>(_GetListOfAlbums_QNAME, GetListOfAlbums.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadPictureResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "uploadPictureResponse")
    public JAXBElement<UploadPictureResponse> createUploadPictureResponse(UploadPictureResponse value) {
        return new JAXBElement<UploadPictureResponse>(_UploadPictureResponse_QNAME, UploadPictureResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteAlbum }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://SOAP.svr.tp1.sd/", name = "deleteAlbum")
    public JAXBElement<DeleteAlbum> createDeleteAlbum(DeleteAlbum value) {
        return new JAXBElement<DeleteAlbum>(_DeleteAlbum_QNAME, DeleteAlbum.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "return", scope = GetPictureDataResponse.class)
    public JAXBElement<byte[]> createGetPictureDataResponseReturn(byte[] value) {
        return new JAXBElement<byte[]>(_GetPictureDataResponseReturn_QNAME, byte[].class, GetPictureDataResponse.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "arg2", scope = UploadPicture.class)
    public JAXBElement<byte[]> createUploadPictureArg2(byte[] value) {
        return new JAXBElement<byte[]>(_UploadPictureArg2_QNAME, byte[].class, UploadPicture.class, ((byte[]) value));
    }

}
