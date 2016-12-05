<%-- 
    Document   : index
    Created on : 18-06-2015, 13:41:37
    Author     : mario
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <title>..:: Messenger Chat Web ::..</title>
        <link href="bootstrap-3.3.7/css/bootstrap.min.css" rel="stylesheet">
        <!-- <link href="bootstrap/css/bootstrap-theme.min.css" rel="stylesheet"> -->
        
    </head>
    
    <body>     
        <div id="msg-post">
        </div>
        <div style="width: 1366px; height: 80px; background-color: #f5f5f5; float: top;">
            <center>
                <img src="images/top.gif" class="img-responsive" alt="Responsive image" width="500">
            </center>
        </div>
        <div style="width: 1366px; height: 525px; background-color: #f5f5f5; ">
            <center>
                <form class="form-group" action="inicio" method="post" name="formCliente" id="formCliente" >
                    <div style="width: 500px; height: 450px; background-color: #f5f5f5; float: none; " class="panel body" id="divFormulario">
                        <div class="form-group has-success">
                            <label class="control-label" for="inputSuccess1">Complete el formulario para empezar con el chat</label>
                        </div>
                        <div class="form-group">
                            <label for="txtNombre">Nombre</label>
                            <input type="text" class="form-control" id="txtNombre" name="txtNombre" placeholder="Juan Perez" >
                        </div>
                        <div class="form-group">
                            <label for="txtTelefono">Telefono</label>
                            <input type="text" class="form-control" id="txtTelefono" name="txtTelefono" placeholder="+569 12345678" >
                        </div>                    
                        <div class="form-group">
                            <label for="txtCorreo">Correo</label>
                            <input type="email" class="form-control" id="txtCorreo" name="txtCorreo" placeholder="correo@dominio.cl" >
                        </div>
                        <div class="form-group">
                            <label for="txtCiudad">Ciudad</label>
                            <input type="text" class="form-control" id="txtCiudad" name="txtCiudad" placeholder="Santiago" >
                        </div>
                        <div class="form-group">
                            <label for="txtAsunto">Asunto</label>
                            <input type="text" class="form-control" id="txtAsunto" name="txtAsunto" placeholder="Cotización" >
                        </div>
                        <div class="form-group">
                            <hr>
                            <button type="button" class="btn btn-success form-control" id="btnInicioChat" onclick="comenzarChat()">Comenzar con el Chat</button>                    
                        </div>

                    </div>
                    <div style="width: 500px; height: 450px; background-color: #f5f5f5; display: none" id="divChat">
                        <!--<div style="width: 820px; height: 450px; background-color: #f5f5f5; float: left;" id="divChat">-->
                        <button type="submit" class="btn btn-danger right form-control" id="btnTerminoChat">Salir del Chat</button>
                        <input type="hidden" id="nombreClienteWeb" name="nombreClienteWeb" value="">
                        <br><textarea  rows="15" readonly  id="areaChat" class="refreshMe"></textarea>
                        <div style="width: 500px; height: 120px; background-color: #f5f5f5;" class="form-inline">
                            <hr><textarea  rows="2" style="width: 500px;" placeholder="Ingrese su Mensaje" readonly class="form-control" id="areaMensajeChat" name="areaMensajeChat"></textarea>
                            <button type="button" class="btn btn-primary form-control" id="btnEnviarMensajeChat" name="btnEnviarMensajeChat">Enviar</button>
                        </div>
                    </div>
                </form>
            </center>
        </div>
        <div style="width: 1366px; height: 50px; background-color: #f5f5f5; float: left;">
            <center>
                <img src="images/bottom.gif" class="img-responsive" alt="Responsive image" width="500">
            </center>
        </div>

        
        
        <script language="javascript" src="jquery/jquery-3.1.1.min.js"></script>
        <script language="javascript" src="jquery/jquery.timers-1.0.0.js"></script>
        <script src="bootstrap-3.3.7/js/bootstrap.min.js"></script>
        <script language="javascript"  type="text/javascript" >
            $(document).ready(function () {
                $("#btnEnviarMensajeChat").click(function () {
                    var nombreClienteWeb = $("#txtNombre").val();
                    var areaMensajeChatWeb = $("#areaMensajeChat").val();
                    $.ajax({//Comunicación jQuery hacia JSP
                        type: "POST",
                        url: "Mensaje",
                        data: "areaMensajeChatWeb=" + areaMensajeChatWeb + "&nombreClienteWeb=" + nombreClienteWeb,
                        success: function (msg) {
                            $("#areaChat").html(msg);
                            var altoAreaText = document.getElementById('areaChat');
                            $("#areaChat").scrollTop(altoAreaText.scrollHeight);
                            $("#areaMensajeChat").val("");
                        },
                        error: function (xml, msg) {
                            $("#areaChat").html("Error: " + msg);
                        }
                    });
                });
                
                $("#btnTerminoChat").click(function () {
                    var nombreClienteWeb = $("#txtNombre").val();                    
                    $.ajax({//Comunicación jQuery hacia JSP
                        type: "POST",
                        url: "Mensaje",
                        data: "nombreClienteWeb=" + nombreClienteWeb,
                        success: function (msg) {
                            
                        },
                        error: function (xml, msg) {
                            
                        }
                    });
                });
                
                $("#btnInicioChat").click(function () {
                    var nombreClienteWeb = $("#txtNombre").val();
//                    var areaMensajeChatWeb = $("#areaMensajeChat").val();
                    $.ajax({//Comunicación jQuery hacia JSP
                        type: "POST",
                        url: "Mensaje",
                        data: "nuevaConexion=true" + "&nombreClienteWeb=" + nombreClienteWeb,
                        success: function (msg) {
                            $("#areaChat").html(msg);
                            var altoAreaText = document.getElementById('areaChat');
                            $("#areaChat").scrollTop(altoAreaText.scrollHeight);
                            $("#areaMensajeChat").val("");
                            $("#nombreClienteWeb").val(nombreClienteWeb);
                        },
                        error: function (xml, msg) {
                            $("#areaChat").html("Error: " + msg);
                        }
                    });
                    $(".refreshMe").everyTime(4000, function (i) {
                        $.ajax({//Comunicación jQuery hacia JSP
                            type: "POST",
                            url: "Mensaje",
                            data: "",
                            success: function (msg) {
                                $("#areaChat").html(msg);
                                var altoAreaText = document.getElementById('areaChat');
                                $("#areaChat").scrollTop(altoAreaText.scrollHeight);

//                                $("#areaChat").add(msg);
//                                $("#areaChat").focus();
//                                $("#areaMensajeChat").focus();
                            },
                            error: function (xml, msg) {
                                $("#areaChat").html("Error: " + msg);
                            }
                        });
                    });
                });

                $("#areaMensajeChat").keypress(function (e) {
                    if (e.which === 13) {

                        var nombreClienteWeb = $("#txtNombre").val();
                        var areaMensajeChatWeb = $("#areaMensajeChat").val();
                        $.ajax({//Comunicación jQuery hacia JSP
                            type: "POST",
                            url: "Mensaje",
                            data: "areaMensajeChatWeb=" + areaMensajeChatWeb + "&nombreClienteWeb=" + nombreClienteWeb,
                            success: function (msg) {
                                $("#areaChat").html(msg);
                                var altoAreaText = document.getElementById('areaChat');
                                $("#areaChat").scrollTop(altoAreaText.scrollHeight);
                                $("#areaMensajeChat").val("");
                            },
                            error: function (xml, msg) {
                                $("#areaChat").html("Error: " + msg);
                            }
                        });
                    }
                });

            });
               
            function validarNumero(event) {
                if (event.keyCode < 48 || event.keyCode > 57) {
                    event.returnValue = false;
                }
            }
            
            function comenzarChat() {
                document.getElementById('divFormulario').style.display = 'none';
                document.getElementById('divChat').style.display = 'block';
                document.getElementById('areaChat').className = 'form-control refreshMe';
                document.getElementById('areaMensajeChat').readOnly = false;

            }
            
        </script>
    </body>
</html>
