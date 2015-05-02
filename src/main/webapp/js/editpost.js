$(document).ready(function() {
    var converter = Markdown.getSanitizingConverter();

    converter.hooks.chain("preConversion", function (text) {
        return text.replace(/===cut===/g, '');
    });

    var editor = new Markdown.Editor(converter);

    editor.run();

    $("#wmd-button-row").append(
        '<li class="wmd-button" style="left: 400px; top: -10px;"><a id="insertSeparator" class="btn btn-default btn-sm">short/full text separator</a> </li>');

    jQuery.fn.extend({
        insertAtCaret: function(myValue){
            return this.each(function(i) {
                if (document.selection) {
                    this.focus();
                    var sel = document.selection.createRange();
                    sel.text = myValue;
                    this.focus();
                }
                else if (this.selectionStart || this.selectionStart == '0') {
                    var startPos = this.selectionStart;
                    var endPos = this.selectionEnd;
                    var scrollTop = this.scrollTop;
                    this.value = this.value.substring(0, startPos)+myValue+this.value.substring(endPos,this.value.length);
                    this.focus();
                    this.selectionStart = startPos + myValue.length;
                    this.selectionEnd = startPos + myValue.length;
                    this.scrollTop = scrollTop;
                } else {
                    this.value += myValue;
                    this.focus();
                }
            });
        }
    });

    $('#insertSeparator').click(function(){
        var $textarea = $('#wmd-input');
        $textarea.insertAtCaret('===cut===');
    });

    $("#postForm").validate({
        rules: {
            title: {
                required: true,
                minlength: 3
            },
            tags: {
                required: true
            },
            text: {
                required: true,
                minlength: 50
            }
        }
    });

    $(window).bind('beforeunload', function(){
        if ($.trim($('#wmd-input').val()) != '')
            return 'You have not submitted your post.';
    });

    $(document).on("submit", "form", function(event){
        $(window).off('beforeunload');
    });
});
